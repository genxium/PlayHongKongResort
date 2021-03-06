import play.test.WithApplication;


/**
 * When using JUnit, extend play.test.WithApplication. Reference https://github.com/playframework/playframework/issues/857
 * */
public class ForeignPartyAccessTokenTest extends WithApplication {
	
		public static final String TAG = ForeignPartyAccessTokenTest.class.getName();

                /*
		@Before
		public void beforeAnything() {
			start();
		}

		@Test
		public void testValidNewAccessToken() {
			HashedMap data = new HashedMap();
			final String accessToken = "qyi32789urjwkqefn";
			final Integer party = ForeignPartyHelper.PARTY_QQ;
			data.put(TempForeignParty.ACCESS_TOKEN, accessToken);
			data.put(TempForeignParty.PARTY, String.valueOf(party));
			final FakeRequest fakeRequest = new FakeRequest(POST, "/player/foreign/login").withFormUrlEncodedBody(data);
			Result result = route(fakeRequest);
			assertThat(status(result)).isEqualTo(OK);

			TempForeignParty record = DBCommander.queryTempForeignParty(accessToken, party);
			assert(record != null);	
			boolean ret = DBCommander.deleteTempForeignParty(accessToken, party);
			assert(ret);
		}
		
		@Test
		public void testValidNewAccessTokenNameCompletion() {
			final String accessToken = "qyi32789urjwkqefn";
			final Integer party = ForeignPartyHelper.PARTY_QQ;

			HashedMap data = new HashedMap();
			data.put(TempForeignParty.ACCESS_TOKEN, accessToken);
			data.put(TempForeignParty.PARTY, String.valueOf(party));

			final FakeRequest fakeRequest1 = new FakeRequest(POST, "/player/foreign/login").withFormUrlEncodedBody(data);
			Result result = route(fakeRequest1);
			assertThat(status(result)).isEqualTo(OK);
			
			data.put(Player.NAME, "valid_unique_tester_name");
			final FakeRequest fakeRequest2 = new FakeRequest(POST, "/player/foreign/login").withFormUrlEncodedBody(data);
			result = route(fakeRequest2);
			assertThat(status(result)).isEqualTo(OK);
	
			// check db records in `perm_foreign_party`, `player`, `login`
			// delete db records in `perm_foreign_party`, `player`, `login`
		}

		@Test
		public void testRegisteredAccessToken() {
			HashedMap data = new HashedMap();
			data.put(TempForeignParty.ACCESS_TOKEN, "a7s89dfhaaskdfja89");
			data.put(TempForeignParty.PARTY, String.valueOf(ForeignPartyHelper.PARTY_QQ));
			final FakeRequest fakeRequest = new FakeRequest(POST, "/player/foreign/login").withFormUrlEncodedBody(data);
			Result result = route(fakeRequest);
			assertThat(status(result)).isEqualTo(OK);

			// check db records in `login`
			// delete db records in `login`
		}
                */
}
