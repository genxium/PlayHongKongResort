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
    public void testValidAccessToken1stStage() {
        HashedMap data = new HashedMap();
        data.put(TempForeignParty.ACCESS_TOKEN, "qyi32789urjwkqefn");
        data.put(TempForeignParty.PARTY, String.valueOf(ForeignPartyController.PARTY_QQ));
        final FakeRequest fakeRequest = new FakeRequest(POST, "/player/foreign/login").withFormUrlEncodedBody(data);
        Result result = route(fakeRequest);
        assertThat(status(result)).isEqualTo(OK);

        /**
         * assertions for db records:
         * 1. temp record created
         * 2. perm record not created
         * */
    }
}
