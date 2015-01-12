package models;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import utilities.Loggy;

import java.util.ArrayList;
import java.util.List;

public class ActivityDetail extends Activity {

    public static String IMAGES = "images";
    public static String APPLIED_PARTICIPANTS = "applied_participants";
    public static String PRESENT_PARTICIPANTS = "present_participants";

    protected List<Image> m_images = null;

    public List<Image> getImages() {
        return m_images;
    }

    public void setImages(List<Image> images) {
        if (m_images != null)   m_images.clear();
        else m_images = new ArrayList<Image>();
        for (Image image : images)  m_images.add(image);
    }

    protected List<BasicUser> m_appliedParticipants = null;
    protected List<BasicUser> m_presentParticipants = null;

    public ActivityDetail(Activity activity, List<Image> images, List<BasicUser> appliedParticipants, List<BasicUser> selectedParticipants, List<BasicUser> presentParticipants) {
        super();
        m_id = activity.getId();
        m_title = activity.getTitle();
        m_content = activity.getContent();
        m_createdTime = activity.getCreatedTime();
        m_beginTime = activity.getBeginTime();
        m_deadline = activity.getDeadline();
        m_capacity = activity.getCapacity();
        m_numApplied = activity.getNumApplied();
        m_numSelected = activity.getNumSelected();
        m_status = activity.getStatus();
        m_host = activity.getHost();
        m_images = images;
        m_appliedParticipants = appliedParticipants;
        m_selectedParticipants = selectedParticipants;
        m_presentParticipants = presentParticipants;
    }

    public ObjectNode toObjectNode(Long viewerId) {
        ObjectNode ret = super.toObjectNode(viewerId);
        try {
            if (m_images != null) {
                ArrayNode imagesNode = new ArrayNode(JsonNodeFactory.instance);
                for (Image image : m_images) {
                    imagesNode.add(image.toObjectNode());
                }
                ret.put(IMAGES, imagesNode);
            }

            ArrayNode appliedParticipantsNode = new ArrayNode(JsonNodeFactory.instance);
            for (BasicUser participant : m_appliedParticipants) {
                appliedParticipantsNode.add(participant.toObjectNode(viewerId));
            }
            ret.put(APPLIED_PARTICIPANTS, appliedParticipantsNode);

            ArrayNode selectedParticipantsNode = new ArrayNode(JsonNodeFactory.instance);
            for (BasicUser participant : m_selectedParticipants) {
                selectedParticipantsNode.add(participant.toObjectNode(viewerId));
            }
            ret.put(SELECTED_PARTICIPANTS, selectedParticipantsNode);

            ArrayNode presentParticipantsNode = new ArrayNode(JsonNodeFactory.instance);
            for (BasicUser participant : m_presentParticipants) {
                if (viewerId != null && viewerId.equals(participant.getId()))	continue; // viewer cannot assess himself/herself
                presentParticipantsNode.add(participant.toObjectNode(viewerId));
            }
            ret.put(PRESENT_PARTICIPANTS, presentParticipantsNode);

            if (m_viewer != null) ret.put(VIEWER, m_viewer.toObjectNode(m_viewer.getId()));

        } catch (Exception e) {
            Loggy.e(TAG, "toObjectNode", e);
        }
        return ret;
    }
}
