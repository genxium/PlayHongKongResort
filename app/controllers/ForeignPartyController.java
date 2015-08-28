package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import components.StandardFailureResult;
import dao.EasyPreparedStatementBuilder;
import dao.SQLHelper;
import exception.ForeignPartyRegistrationRequiredException;
import exception.TempForeignPartyRecordNotFoundException;
import exception.PlayerNotFoundException;
import fixtures.Constants;
import models.Login;
import models.PermForeignParty;
import models.TempForeignParty;
import models.Player;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import play.libs.Json;
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
				if (Play.application().isProd())	return null; 
				else if (Play.application().isDev())	return null;
				else {
					Map<String, Long> testAccessTokenMap = new HashMap<>();
					testAccessTokenMap.put("qyi32789urjwkqefn", 12345678L); // should NOT be in test table `perm_foreign_party`
					testAccessTokenMap.put("a7s89dfhaaskdfja89", 87654321L); // should be in test table `perm_foreign_party` and `player`
					if (!testAccessTokenMap.containsKey(accessToken)) return null;
					return new ForeignPartySpecs(testAccessTokenMap.get(accessToken), party, null);
				}
			default:
				return null;
		}
	}

	protected static Player loginWithNameCompletion(final String accessToken, final Integer party, final String name, String email) throws ForeignPartyRegistrationRequiredException, TempForeignPartyRecordNotFoundException, SQLException, NullPointerException {

		// player should re-submit valid name and email(if not empty) 
		if (name == null || !General.validateName(name)) throw new ForeignPartyRegistrationRequiredException();
		if (email != null && !General.validateEmail(email)) throw new ForeignPartyRegistrationRequiredException();

		TempForeignParty tempForeignPartyRecord = DBCommander.queryTempForeignParty(accessToken, party);

		// player should re-submit access-token and party-id 
		if (tempForeignPartyRecord == null)	throw new TempForeignPartyRecordNotFoundException(); 

		if (email == null) email = tempForeignPartyRecord.getEmail(); 

		Connection connection = SQLHelper.getConnection();
		if (connection == null)	throw new NullPointerException(); 
	
		Long playerId = null;

		/**
		 * TODO: clean up these codes
		 * Transaction begins
		 * */

		boolean transactionSucceeded = true;

		SQLHelper.disableAutoCommit(connection);
		try {

			// insert record into `player`

			String code = DBCommander.generateVerificationCode(name);

			String[] cols = {Player.EMAIL, Player.NAME, Player.PASSWORD, Player.GROUP_ID, Player.PARTY, Player.VERIFICATION_CODE};
			Object[] values = {email, name, "", Player.USER, party, code};

			EasyPreparedStatementBuilder createPlayerBuilder = new EasyPreparedStatementBuilder();
			PreparedStatement createPlayerStat = createPlayerBuilder.insert(cols, values)
										.into(Player.TABLE)
										.toInsert(connection);

			playerId = SQLHelper.executeInsertAndCloseStatement(createPlayerStat);

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
				.where(TempForeignParty.PARTY, "=", tempForeignPartyRecord.getParty())
				.toDelete(connection);
			SQLHelper.executeAndCloseStatement(deleteTempForeignPartyStat);

		} catch (SQLException e) {
			transactionSucceeded = false;
			SQLHelper.rollback(connection);
			Loggy.e(TAG, "loginWithNameCompletion", e);
		} catch (Exception e) {
			Loggy.e(TAG, "loginWithNameCompletion", e);
		} finally {
			SQLHelper.enableAutoCommitAndClose(connection);
		}
		/**
		 * Transaction ends
		 * */

		if (!transactionSucceeded)	throw new NullPointerException();
		Player player = new Player(email, name);
		/**
		 * TODO: the following 2 lines are used for adaptation of Player.toObjectNode method, however this might be better covered by the initialization of `Player`
		 * */
		player.setId(playerId);
		player.setParty(party);
		player.setGroupId(Player.USER);
		return player;
		/**
		 * TODO: send verification email to player, but by far `foreign party account verified` and `email verified` states are not separated
		 * */
	}

	protected static Player loginWithoutNameCompletion(final String accessToken, final Integer party) throws ForeignPartyRegistrationRequiredException {
		ForeignPartySpecs specs = queryForeignPartySpecs(accessToken, party);
		if (specs == null || !specs.isValid())	return null; 

		PermForeignParty record = DBCommander.queryPermForeignParty(specs.id, party);
		if (record != null)	return DBCommander.queryPlayer(record.getPlayerId());
		
		// player should submit valid name and email(if not empty)
		DBCommander.createTempForeignParty(accessToken, party, specs.id, specs.email);
		throw new ForeignPartyRegistrationRequiredException();
	}

	public static Result login() {
		try {
			Map<String, String[]> formData = request().body().asFormUrlEncoded();
			if (!formData.containsKey(TempForeignParty.ACCESS_TOKEN) || !formData.containsKey(TempForeignParty.PARTY))  return ok(StandardFailureResult.get());

			String accessToken = formData.get(TempForeignParty.ACCESS_TOKEN)[0];
			Integer party = Converter.toInteger(formData.get(TempForeignParty.PARTY)[0]);

			String name = (formData.containsKey(Player.NAME) ? formData.get(Player.NAME)[0] : null);
			String email = (formData.containsKey(Player.EMAIL) ? formData.get(Player.EMAIL)[0] : null);
			Player player = null;

			if (name != null)	player = loginWithNameCompletion(accessToken, party, name, email);
			else	player = loginWithoutNameCompletion(accessToken, party);

			if (player == null) throw new PlayerNotFoundException();
	
			// auto-login
			String token = Converter.generateToken(player.getEmail(), player.getName());

			EasyPreparedStatementBuilder builder = new EasyPreparedStatementBuilder();
			String[] cols = {Login.PLAYER_ID, Login.TOKEN, Login.TIMESTAMP};
			Object[] vals = {player.getId(), token, General.millisec()};
			builder.insert(cols, vals).into(Login.TABLE).execInsert();

			ObjectNode result = Json.newObject();
			result.put(Player.TABLE, player.toObjectNode(player.getId()));
			result.put(Player.TOKEN, token);

			return ok(result);
		} catch (TempForeignPartyRecordNotFoundException e) {
			return ok(StandardFailureResult.get(Constants.INFO_TEMP_FOREIGN_PARTY_RECORD_NOT_FOUND));
		} catch (ForeignPartyRegistrationRequiredException e) {
			return ok(StandardFailureResult.get(Constants.INFO_FOREIGN_PARTY_REGISTRATION_REQUIRED));	
		} catch (PlayerNotFoundException e) {
			return ok(StandardFailureResult.get(Constants.INFO_PLAYER_NOT_FOUND));	
		} catch (Exception e) {
			Loggy.e(TAG, "login", e);
		}
		return badRequest();
	}
}
