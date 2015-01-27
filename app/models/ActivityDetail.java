package models;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import utilities.Loggy;

import java.util.ArrayList;
import java.util.List;

public class ActivityDetail extends Activity {

    public static String APPLIED_PARTICIPANTS = "applied_participants";
    public static String PRESENT_PARTICIPANTS = "present_participants";

    protected List<Image> m_images = null;

    public List<Image> getImages() {
        return m_images;
    }

    public void setImages(List<Image> images) {
        m_images = images;
    }

    protected List<BasicUser> m_appliedParticipants = null;
    public void setAppliedParticipants(final List<BasicUser> appliedParticipants) {
        m_appliedParticipants = appliedParticipants;
    }
    public void addAppliedParticipant(final BasicUser user) {
        if (m_appliedParticipants == null) m_appliedParticipants = new ArrayList<>();
        m_appliedParticipants.add(user);
    }

    protected List<BasicUser> m_presentParticipants = null;
    public void setPresentParticipants(final List<BasicUser> presentParticipants) {
        m_presentParticipants = presentParticipants;
    }
    public void addPresentParticipant(final BasicUser user) {
        if (m_presentParticipants == null) m_presentParticipants = new ArrayList<>();
        m_presentParticipants.add(user);
    }

    public ActivityDetail(final JSONObject activityJson) {
        super(activityJson);
    }

    public ObjectNode toObjectNode(Long viewerId) {
        ObjectNode ret = super.toObjectNode(viewerId);
        try {

            ArrayNode appliedParticipantsNode = new ArrayNode(JsonNodeFactory.instance);
            for (BasicUser participant : m_appliedParticipants) {
                appliedParticipantsNode.add(participant.toObjectNode(viewerId));
            }
            ret.put(APPLIED_PARTICIPANTS, appliedParticipantsNode);

            ArrayNode presentParticipantsNode = new ArrayNode(JsonNodeFactory.instance);
            for (BasicUser participant : m_presentParticipants) {
                if (viewerId != null && viewerId.equals(participant.getId()))	continue; // viewer cannot assess himself/herself
                presentParticipantsNode.add(participant.toObjectNode(viewerId));
            }
            ret.put(PRESENT_PARTICIPANTS, presentParticipantsNode);

            if (m_viewer != null) ret.put(VIEWER, m_viewer.toObjectNode(null));

        } catch (Exception e) {
            Loggy.e(TAG, "toObjectNode", e);
        }
        return ret;
    }
}
