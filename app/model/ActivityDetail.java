package model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActivityDetail extends Activity {

	public static String IMAGES ="images";
	public static String APPLIED_PARTICIPANTS ="applied_participants";
	public static String SELECTED_PARTICIPANTS ="selected_participants";

	protected List<Image> m_images=null;
	public List<Image> getImages() {return m_images;}
	public void setImages(List<Image> images){
		if(m_images!=null){
		    m_images.clear();
		} else{
		    m_images=new ArrayList<Image>();
		}
		Iterator<Image> it=images.iterator();
		while(it.hasNext()){
		    Image image=it.next();
		    m_images.add(image);
		}
	}
	
	protected List<BasicUser> m_appliedParticipants=null;
	public List<BasicUser> getAppliedParticipants() {return m_appliedParticipants;}
	public void setAppliedParticipants(List<BasicUser> appliedParticipants){
		if(m_appliedParticipants!=null){
			m_appliedParticipants.clear();
		} else{
			m_appliedParticipants=new ArrayList<BasicUser>();
		}
		Iterator<BasicUser> it=appliedParticipants.iterator();
		while(it.hasNext()){
			BasicUser participant=it.next();
			m_appliedParticipants.add(participant);
		}
	}
	
	protected List<BasicUser> m_selectedParticipants=null;
	public List<BasicUser> getSelectedParticipants() {return m_selectedParticipants;}
	public void setSelectedParticipants(List<BasicUser> selectedParticipants){
		if(m_selectedParticipants!=null){
			m_selectedParticipants.clear();
		} else{
			m_selectedParticipants=new ArrayList<BasicUser>();
		}
		Iterator<BasicUser> it=selectedParticipants.iterator();
		while(it.hasNext()){
			BasicUser participant=it.next();
			m_selectedParticipants.add(participant);
		}
	}

    	public ActivityDetail(Activity activity, List<Image> images, List<BasicUser> appliedParticipants, List<BasicUser> selectedParticipants){
		m_id=activity.getId();
		m_title=activity.getTitle();
		m_content=activity.getContent();
		m_createdTime=activity.getCreatedTime();
		m_beginTime=activity.getBeginTime();
		m_deadline=activity.getDeadline();
		m_capacity=activity.getCapacity();
		m_status=activity.getStatus();
		m_images=images;
		m_appliedParticipants=appliedParticipants;
		m_selectedParticipants=selectedParticipants;
    	}

	public ActivityDetail(JSONObject activityJson, List<Image> images,
		List<BasicUser> appliedParticipants, List<BasicUser> selectedParticipants) {
		super(activityJson);
		m_images=images;
		m_appliedParticipants=appliedParticipants;
		m_selectedParticipants=selectedParticipants;
	}
	
	public ObjectNode toObjectNode(Integer viewerId){
		ObjectNode ret = null;
        	try{
			ret=super.toObjectNode(viewerId);

			if(m_images!=null){
				ArrayNode imagesNode=new ArrayNode(JsonNodeFactory.instance);
				for(Image image : m_images){
					imagesNode.add(image.toObjectNode());
				}
				ret.put(ActivityDetail.IMAGES, imagesNode);
			}
			
			if(m_appliedParticipants!=null && m_appliedParticipants.size()>0){
				ArrayNode appliedParticipantsNode=new ArrayNode(JsonNodeFactory.instance);
				Iterator<BasicUser> itParticipant=m_appliedParticipants.iterator();
				while(itParticipant.hasNext()){
					ObjectNode singleParticipantNode=Json.newObject();
					BasicUser participant=itParticipant.next();
					singleParticipantNode.put(BasicUser.ID, participant.getUserId());
					singleParticipantNode.put(BasicUser.EMAIL, participant.getEmail());
					singleParticipantNode.put(BasicUser.NAME, participant.getName());
					appliedParticipantsNode.add(singleParticipantNode);
				}
				ret.put(ActivityDetail.APPLIED_PARTICIPANTS, appliedParticipantsNode);
			}
			
			if(m_selectedParticipants!=null && m_selectedParticipants.size()>0){
				ArrayNode  selectedParticipantsNode=new ArrayNode(JsonNodeFactory.instance);
				Iterator<BasicUser> itParticipant=m_selectedParticipants.iterator();
				while(itParticipant.hasNext()){
					ObjectNode singleParticipantNode=Json.newObject();
					BasicUser participant=itParticipant.next();
				    	singleParticipantNode.put(BasicUser.ID, participant.getUserId());
					singleParticipantNode.put(BasicUser.EMAIL, participant.getEmail());
					singleParticipantNode.put(BasicUser.NAME, participant.getName());
					selectedParticipantsNode.add(singleParticipantNode);
				}
				ret.put(ActivityDetail.SELECTED_PARTICIPANTS, selectedParticipantsNode);
			}
		} catch (Exception e) {
			System.out.println("ActivityDetail.toObjectNode, "+e.getMessage());
		}
		return ret;
	}
}
