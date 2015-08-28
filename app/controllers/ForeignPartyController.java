package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import components.StandardFailureResult;
import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.InvalidRegistrationParamsException;
import exception.PlayerNotFoundException;
import fixtures.Constants;
import models.Login;
import models.PermForeignParty;
import models.TempForeignParty;
import models.Player;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Converter;
import utilities.General;
import utilities.Loggy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class ForeignPartyController extends Controller {

    public static String TAG = ForeignPartyController.class.getName();

    public static final int PARTY_NONE = 0;
    public static final int PARTY_QQ = 1;

    public static final HashMap<Integer, String> PARTY_DEFAULT_DOMAIN_NAME = new HashMap<>();
    static {
        PARTY_DEFAULT_DOMAIN_NAME.put(PARTY_QQ, "qq.com");
    }

    public static class ForeignPartySpecs {
        public String TAG = ForeignPartySpecs.class.getName();
        public Long id = null;
        public Integer party = null;
        public String email = null;
        public ForeignPartySpecs(final Long aId, final Integer aParty, final String aEmail) {
            id = aId;
			party = aParty;
            if (aEmail == null) email = String.format("%d@%s", aId, PARTY_DEFAULT_DOMAIN_NAME.get(aParty));
            else email = aEmail;
        }
        public boolean isValid() {
            return (id != null && party != null);
        }
    }

    protected static ForeignPartySpecs queryForeignPartySpecs (final String accessToken, final Integer party) {
        /**
         * TODO: implementation for major foreign parties
         * */
        switch (party) {
            case PARTY_QQ:
                return new ForeignPartySpecs(12345678L, party, null);
            default:
                return null;
        }
    }

    public static Result login() {
        try {
            Map<String, String[]> formData = request().body().asFormUrlEncoded();
            if (!formData.containsKey(TempForeignParty.ACCESS_TOKEN) || !formData.containsKey(TempForeignParty.PARTY))  return ok(StandardFailureResult.get());

            String accessToken = formData.get(TempForeignParty.ACCESS_TOKEN)[0];
            Integer party = Converter.toInteger(formData.get(TempForeignParty.PARTY)[0]);
            Player player = null;

            if (formData.containsKey(Player.NAME) && formData.containsKey(Player.EMAIL)) {

                String name = formData.get(Player.NAME)[0];
                String email = formData.get(Player.EMAIL)[0];

                if ((name == null || !General.validateName(name)) || (email == null || !General.validateEmail(email)) )  throw new InvalidRegistrationParamsException();

                TempForeignParty tempForeignPartyRecord = DBCommander.queryTempForeignParty(accessToken, party);
                if (tempForeignPartyRecord == null) return ok(StandardFailureResult.get());

                /**
                 * TODO: clean up these codes
                 * Transaction begins
                 * */
                boolean transactionSucceeded = true;
                Connection connection = SQLHelper.getConnection();
                SQLHelper.disableAutoCommit(connection);
                try {
                    if (connection == null) throw new NullPointerException();

                    // insert record into `player`

                    String code = DBCommander.generateVerificationCode(name);

                    String[] cols = {Player.EMAIL, Player.NAME, Player.GROUP_ID, Player.VERIFICATION_CODE};
                    Object[] values = {email, name, Player.USER, code};

                    EasyPreparedStatementBuilder createPlayerBuilder = new EasyPreparedStatementBuilder();
                    PreparedStatement createPlayerStat = createPlayerBuilder.insert(cols, values)
                            .into(Player.TABLE)
                            .toInsert(connection);

                    Long playerId = SQLHelper.executeInsertAndCloseStatement(createPlayerStat);

                    // insert record into `perm_foreign_party`
                    String[] cols2 = {PermForeignParty.ID, PermForeignParty.PARTY, PermForeignParty.PLAYER_ID};
                    Object[] vals2 = {tempForeignPartyRecord.getPartyId(), tempForeignPartyRecord.getParty(), playerId};

                    EasyPreparedStatementBuilder createPermForeignPartyBuilder = new EasyPreparedStatementBuilder();
                    PreparedStatement createPermForeignPartyStat = createPermForeignPartyBuilder.insert(cols2, vals2)
                            .into(PermForeignParty.TABLE)
                            .toInsert(connection);

                    SQLHelper.executeAndCloseStatement(createPermForeignPartyStat);

                    // remove record from `temp_foreign_party`
                    EasyPreparedStatementBuilder deleteTempForeignPartyBuilder = new EasyPreparedStatementBuilder();
                    PreparedStatement deleteTempForeignPartyStat = deleteTempForeignPartyBuilder.from(TempForeignParty.TABLE)
                            .where(TempForeignParty.ACCESS_TOKEN, "=", tempForeignPartyRecord.getAccessToken())
                            .toDelete(connection);
                    SQLHelper.executeAndCloseStatement(deleteTempForeignPartyStat);

                } catch (SQLException e) {
                    transactionSucceeded = false;
                    SQLHelper.rollback(connection);
                    Loggy.e(TAG, "login", e);
                } catch (Exception e) {
                    Loggy.e(TAG, "login", e);
                } finally {
                    SQLHelper.enableAutoCommitAndClose(connection);
                }
                /**
                 * Transaction ends
                 * */

                if (transactionSucceeded) {
                    player = new Player(email, name);
                    /**
                     * TODO: the following 2 lines are used for adaptation of Player.toObjectNode method, however this might be better covered by the initialization of `Player`
                     * */
                    player.setParty(party);
                    player.setGroupId(Player.USER);
                    /**
                     * TODO: send verification email to player, but by far `foreign party account verified` and `email verified` states are not separated
                     * */
                }
            } else {
                ForeignPartySpecs specs = queryForeignPartySpecs(accessToken, party);
                if (specs == null || !specs.isValid()) return ok(StandardFailureResult.get());

                PermForeignParty record = DBCommander.queryPermForeignParty(specs.id, party);
                if (record == null) {
                    DBCommander.createTempForeignParty(accessToken, party, specs.id, specs.email);
                    return ok(StandardFailureResult.get(Constants.INFO_FOREIGN_PARTY_REGISTRATION_REQUIRED));
                }
                player = DBCommander.queryPlayer(record.getPlayerId());
            }

            if (player == null) throw new PlayerNotFoundException();

            String token = Converter.generateToken(player.getEmail(), player.getName());

            EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
            String[] cols = {Login.PLAYER_ID, Login.TOKEN, Login.TIMESTAMP};
            Object[] vals = {player.getId(), token, General.millisec()};
            builder.insert(cols, vals).into(Login.TABLE).execInsert();
            ObjectNode result = player.toObjectNode(player.getId());
            result.put(Player.TOKEN, token);

            return ok(result);
        } catch (Exception e) {
            Loggy.e(TAG, "login", e);
        }
        return badRequest();
    }
}
