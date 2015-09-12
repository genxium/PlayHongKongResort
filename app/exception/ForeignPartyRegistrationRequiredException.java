package exception;

public class ForeignPartyRegistrationRequiredException extends Exception {

        private String partyNickname = null; // for QQ only
        public String getPartyNickname() {
                return partyNickname;
        }

        public ForeignPartyRegistrationRequiredException(final String data) {
                super("Foreign party registration required");
                partyNickname = data;
        }

} 
