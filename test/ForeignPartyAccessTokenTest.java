import controllers.ForeignPartyController;
import models.TempForeignParty;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

import play.mvc.Result;
import play.test.FakeRequest;
import play.test.WithApplication;

import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;


/**
 * When using JUnit, extend play.test.WithApplication. Reference https://github.com/playframework/playframework/issues/857
 * */
public class ForeignPartyAccessTokenTest extends WithApplication {

		@Before
		public void beforeAnything() {
			start();
		}

		@Test
		public void testValidNewAccessToken() {
			HashedMap data = new HashedMap();
			data.put(TempForeignParty.ACCESS_TOKEN, "qyi32789urjwkqefn");
			data.put(TempForeignParty.PARTY, String.valueOf(ForeignPartyController.PARTY_QQ));
			final FakeRequest fakeRequest = new FakeRequest(POST, "/player/foreign/login").withFormUrlEncodedBody(data);
			Result result = route(fakeRequest);
			assertThat(status(result)).isEqualTo(OK);

			/**
			 * check db records in 'temp_foreign_party'
			 * */
		}

		@Test
		public void testValidNewAccessTokenNameCompletion() {
			HashedMap data = new HashedMap();
			data.put(TempForeignParty.ACCESS_TOKEN, "qyi32789urjwkqefn");
			data.put(TempForeignParty.PARTY, String.valueOf(ForeignPartyController.PARTY_QQ));
			data.put(TempForeignParty.NAME, "valid_unique_tester_name");
			final FakeRequest fakeRequest = new FakeRequest(POST, "/player/foreign/login").withFormUrlEncodedBody(data);
			Result result = route(fakeRequest);
			assertThat(status(result)).isEqualTo(OK);
	
			/**
			 * check db records in `perm_foreign_party`, `player`, `login`
			 * delete db records in `perm_foreign_party`, `player`, `login`
			 * */
		}

		@Test
		public void testRegisteredAccessToken() {
			HashedMap data = new HashedMap();
			data.put(TempForeignParty.ACCESS_TOKEN, "a7s89dfhaaskdfja89");
			data.put(TempForeignParty.PARTY, String.valueOf(ForeignPartyController.PARTY_QQ));
			final FakeRequest fakeRequest = new FakeRequest(POST, "/player/foreign/login").withFormUrlEncodedBody(data);
			Result result = route(fakeRequest);
			assertThat(status(result)).isEqualTo(OK);

			/**
			 * check db records in `login`
			 * delete db records in `login`
			 * */
		}
}
