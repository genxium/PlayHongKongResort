package models;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.SimpleMap;
import utilities.Loggy;

import java.util.ArrayList;
import java.util.List;

public class ActivityDetail extends Activity {

        public static String APPLIED_PARTICIPANTS = "applied_participants";
        public static String PRESENT_PARTICIPANTS = "present_participants";

        protected List<BasicPlayer> appliedParticipants = null;
        public void addAppliedParticipant(final BasicPlayer player) {
                if (appliedParticipants == null) appliedParticipants = new ArrayList<>();
                appliedParticipants.add(player);
        }

        protected List<BasicPlayer> presentParticipants = null;
        public void addPresentParticipant(final BasicPlayer player) {
                if (presentParticipants == null) presentParticipants = new ArrayList<>();
                presentParticipants.add(player);
        }

        public ActivityDetail(final SimpleMap data) {
                super(data);
        }

        public ObjectNode toObjectNode(final Long viewerId) {
                final ObjectNode ret = super.toObjectNode(viewerId);
                try {
			if (appliedParticipants != null) {
				ArrayNode appliedParticipantsNode = new ArrayNode(JsonNodeFactory.instance);
				for (BasicPlayer participant : appliedParticipants) {
					appliedParticipantsNode.add(participant.toObjectNode(viewerId));
				}
				ret.put(APPLIED_PARTICIPANTS, appliedParticipantsNode);
			}

			if (presentParticipants != null) {
				ArrayNode presentParticipantsNode = new ArrayNode(JsonNodeFactory.instance);
				for (BasicPlayer participant : presentParticipants) {
					if (viewerId != null && viewerId.equals(participant.getId()))   continue;
					presentParticipantsNode.add(participant.toObjectNode(viewerId));
				}
				ret.put(PRESENT_PARTICIPANTS, presentParticipantsNode);
			}
                        if (viewer != null) ret.put(VIEWER, viewer.toObjectNode(null));

                } catch (Exception e) {
                        Loggy.e(TAG, "toObjectNode", e);
                }
                return ret;
        }
}
