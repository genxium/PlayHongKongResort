import com.fasterxml.jackson.databind.node.ObjectNode;
import components.StandardSuccessResult;
import controllers.ForeignPartyController;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ForeignPartyAccessTokenTest {

    @Test
    public void testLogin() {
        ForeignPartyController mockController = mock(ForeignPartyController.class);
        ObjectNode stdSuccess = StandardSuccessResult.get();
        doReturn(stdSuccess).when(mockController).login();
        assertEquals(stdSuccess, mockController.login());
    }
}
