package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import components.StandardFailureResult;
import dao.SQLBuilder;
import dao.SQLHelper;
import exception.ForeignPartyRegistrationRequiredException;
import exception.PlayerNotFoundException;
import exception.TempForeignPartyRecordNotFoundException;
import fixtures.Constants;
import models.Login;
import models.PermForeignParty;
import models.Player;
import models.TempForeignParty;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ForeignPartyController extends Controller {

	public static String TAG = ForeignPartyController.class.getName();

	public static class ForeignPartySpecs {
		public String TAG = ForeignPartySpecs.class.getName();
		public String id = null;
		public Integer party = null;
		public String email = null;

		public ForeignPartySpecs(final String aId, final Integer aParty) {
			id = aId;
			party = aParty;
		}

		public boolean isValid() {
			return (id != null && party != null);
		}
	}

	public static class WrappedPlayer {
	        public Player player = null;
	        public String partyId = null;

                // ONLY used in QQ at the moment
                public static final String PARTY_NICKNAME = "party_nickname";
                public String partyNickname = null;

	        public WrappedPlayer(final Player aPlayer, final String aPartyId) {
	                player = aPlayer;
	                partyId = aPartyId;
	        }
	        public ObjectNode toObjectNode(final Long viewerId) {
	                final ObjectNode ret = player.toObjectNode(viewerId);
                        if (partyNickname != null) ret.put(PARTY_NICKNAME, partyNickname);
                        return ret;
	        }
	}

	protected static String queryQQNickname(final String accessToken, final String openId) {
	        try {
                        final Map<String, String> qqAttr = ForeignPartyHelper.getAttr(ForeignPartyHelper.PARTY_QQ);
                        if (qqAttr == null) return null;
                        final Map<String, Object> params = new HashMap<>();
                        params.put("oauth_consumer_key", qqAttr.get(ForeignPartyHelper.APP_ID));
                        params.put(TempForeignParty.ACCESS_TOKEN, accessToken);
                        params.put("openid", openId);
                        params.put("format", "json");
                        final String url = "https://graph.qq.com/user/get_user_info?" + DataUtils.toUrlParams(params);
                        final URLConnection conn = new URL(url).openConnection();
                        final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line = "";
                        String tmp;
                        while ((tmp = in.readLine()) != null) {
                                line += tmp;
                        }
                        in.close();
                        final ObjectMapper mapper = new ObjectMapper();
                        final Map<String, String> parsedData = mapper.readValue(line, mapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
                        return parsedData.get("nickname");
                } catch (Exception e) {
                        Loggy.e(TAG, "queryQQNickname", e);
                }
                return null;
	}

	protected static ForeignPartySpecs queryForeignPartySpecs(final String accessToken, final Integer party, String partyId) throws IOException {
		/**
		 * TODO: implementation for major foreign parties
		 * */
		switch (party) {
			case ForeignPartyHelper.PARTY_QQ:
                                final Map<String, Object> params = new HashMap<>();
                                params.put(TempForeignParty.ACCESS_TOKEN, accessToken);
				String url = "https://graph.qq.com/oauth2.0/me?" + DataUtils.toUrlParams(params);
                                final URLConnection conn = new URL(url).openConnection();
				final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				final String line = in.readLine();
				in.close();

				Pattern resPattern = Pattern.compile("^callback\\([\\s\\S]*\\{\"client_id\":\"([\\w\\d]+)\",\"openid\":\"([\\w\\d]+)\"\\}[\\s\\S]*\\);$", Pattern.UNICODE_CHARACTER_CLASS);
				Matcher matcher = resPattern.matcher(line);
				if (!matcher.matches()) return null;
				partyId = matcher.group(2);
				return new ForeignPartySpecs(partyId, party);
			default:
				return null;
		}
	}

	protected static WrappedPlayer loginWithNameCompletion(final String accessToken, final Integer party, String partyId, final String name, String email) throws ForeignPartyRegistrationRequiredException, TempForeignPartyRecordNotFoundException, SQLException, NullPointerException {

		// player should re-submit valid name and email(if not empty)
		if (name == null || !General.validateName(name)) throw new ForeignPartyRegistrationRequiredException();
		if (email != null && !General.validateEmail(email)) throw new ForeignPartyRegistrationRequiredException();

		final TempForeignParty tempForeignPartyRecord = DBCommander.queryTempForeignParty(accessToken, party);

		// player should re-submit access-token and party-id
		if (tempForeignPartyRecord == null) throw new TempForeignPartyRecordNotFoundException();

                if (partyId == null) partyId = tempForeignPartyRecord.getPartyId();

		final Connection connection = SQLHelper.getConnection();
		if (connection == null) throw new NullPointerException();

		Long playerId = null;

		/**
		 * TODO: clean up these codes
		 * Transaction begins
		 * */

		boolean transactionSucceeded = true;

		SQLHelper.disableAutoCommit(connection);
		try {

			// insert record into `player`
			final String code = DBCommander.generateVerificationCode(name);

			final String[] cols = {Player.EMAIL, Player.NAME, Player.GROUP_ID, Player.PARTY, Player.VERIFICATION_CODE};
			final Object[] values = {email, name, Player.USER, party, code};

			final SQLBuilder createPlayerBuilder = new SQLBuilder();
			final PreparedStatement createPlayerStat = createPlayerBuilder.insert(cols, values)
										.into(Player.TABLE)
										.toInsert(connection);

			playerId = SQLHelper.executeInsertAndCloseStatement(createPlayerStat);

			// insert record into `perm_foreign_party`
			final String[] cols2 = {PermForeignParty.ID, PermForeignParty.PARTY, PermForeignParty.PLAYER_ID};
			final Object[] vals2 = {tempForeignPartyRecord.getPartyId(), tempForeignPartyRecord.getParty(), playerId};

			final SQLBuilder createPermForeignPartyBuilder = new SQLBuilder();
			final PreparedStatement createPermForeignPartyStat = createPermForeignPartyBuilder.insert(cols2, vals2)
                                                                                                .into(PermForeignParty.TABLE)
                                                                                                .toInsert(connection);

			SQLHelper.executeAndCloseStatement(createPermForeignPartyStat);

			// remove record from `temp_foreign_party`
			final SQLBuilder deleteTempForeignPartyBuilder = new SQLBuilder();
			final PreparedStatement deleteTempForeignPartyStat = deleteTempForeignPartyBuilder.from(TempForeignParty.TABLE)
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

		if (!transactionSucceeded) throw new NullPointerException();
		final Player player = new Player(email, name);
                if (playerId == null) throw new NullPointerException();
		player.setId(playerId);
		player.setParty(party);
		player.setGroupId(Player.USER);
		return new WrappedPlayer(player, partyId);
		/**
		 * TODO: send verification email to player, but by far `foreign party account verified` and `email verified` states are not separated
		 * */
	}

	protected static WrappedPlayer loginWithoutNameCompletion(final String accessToken, final Integer party, String partyId) throws ForeignPartyRegistrationRequiredException, IOException {
	        if (partyId == null) {

	                // for implicit-grant
                        final ForeignPartySpecs specs = queryForeignPartySpecs(accessToken, party, partyId);
                        if (specs == null || !specs.isValid()) return null;
                        partyId = specs.id;
                }

		final PermForeignParty record = DBCommander.queryPermForeignParty(partyId, party);

		if (record != null) {
		        final Player player = DBCommander.queryPlayer(record.getPlayerId());
                        return new WrappedPlayer(player, partyId);
                }

		// record creation failure might indicate that there's an existing record
		DBCommander.createTempForeignParty(accessToken, party, partyId);

		// player should submit valid name and email(if not empty)
		throw new ForeignPartyRegistrationRequiredException();
	}

	public static Result login(String grantType) {
		try {

			final Map<String, String[]> formData = request().body().asFormUrlEncoded();
			if (!formData.containsKey(TempForeignParty.ACCESS_TOKEN) || !formData.containsKey(TempForeignParty.PARTY))	throw new NullPointerException();	

			final Integer party = Converter.toInteger(formData.get(TempForeignParty.PARTY)[0]);

			String accessToken = null;
			String partyId = null;
			if (grantType.equals("authcode")) {
				final String authorizationCode = (formData.containsKey(TempForeignParty.AUTHORIZATION_CODE) ? formData.get(TempForeignParty.AUTHORIZATION_CODE)[0] : null);
				accessToken = (formData.containsKey(TempForeignParty.ACCESS_TOKEN) ? formData.get(TempForeignParty.ACCESS_TOKEN)[0] : null);
				if (authorizationCode != null) {
					// TODO: get (access token, party id) for specified party if authorization code is not empty 

				} 
				return ok(StandardFailureResult.get());
			}
			if (grantType.equals("implicit")) {
				accessToken = formData.get(TempForeignParty.ACCESS_TOKEN)[0];
			}
			final String name = (formData.containsKey(Player.NAME) ? formData.get(Player.NAME)[0] : null);
			final String email = (formData.containsKey(Player.EMAIL) ? formData.get(Player.EMAIL)[0] : null);
			WrappedPlayer wrappedPlayer = null;

			if (name != null) wrappedPlayer = loginWithNameCompletion(accessToken, party, partyId, name, email);
			else wrappedPlayer = loginWithoutNameCompletion(accessToken, party, partyId);

			if (wrappedPlayer == null) throw new PlayerNotFoundException();


                        // TODO: clear this dirty fix
                        if (party == ForeignPartyHelper.PARTY_QQ) {
                                final String nickname = queryQQNickname(accessToken, wrappedPlayer.partyId);
                                // if (nickname == null) throw new NullPointerException();
                                wrappedPlayer.partyNickname = nickname;
                        }


			// auto-login
			final String token = Converter.generateToken(wrappedPlayer.player.getEmail(), wrappedPlayer.player.getName());

			final SQLBuilder builder = new SQLBuilder();
			final String[] cols = {Login.PLAYER_ID, Login.TOKEN, Login.TIMESTAMP};
			final Object[] vals = {wrappedPlayer.player.getId(), token, General.millisec()};
			builder.insert(cols, vals).into(Login.TABLE).execInsert();

			final ObjectNode result = wrappedPlayer.toObjectNode(wrappedPlayer.player.getId());
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
			return ok(StandardFailureResult.get());
		}
	}
}
