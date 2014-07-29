package model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import controllers.SQLCommander;

public class ActivityDetail extends Activity {

	public static String IMAGES ="images";
	public static String APPLIED_PARTICIPANTS="applied_participants";
	public static String SELECTED_PARTICIPANTS="selected_participants";
	public static String PRESENT_PARTICIPANTS="present_participants";
	public static String HOST_NAME="host_name";

	protected List<Image> m_images=null;
	public List<Image> getImages() {return m_images;}
	public void setImages(List<Image> images){
		if(m_images!=null){
		    m_images.clear();
		} else{
		    m_images=new ArrayList<Image>();
		}
		for(Image image : images){
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
		for(BasicUser participant : appliedParticipants){
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
		for(BasicUser participant : selectedParticipants){
			m_selectedParticipants.add(participant);
		}
	}

	protected List<BasicUser> m_presentParticipants=null;
	public List<BasicUser> getPresentParticipants() {return m_presentParticipants;}
	public void setPresentParticipants(List<BasicUser> presentParticipants){
		if(m_presentParticipants!=null){
			m_presentParticipants.clear();		
		} else{
			m_presentParticipants=new ArrayList<BasicUser>();
		}
		for(BasicUser participant : presentParticipants){
			m_presentParticipants.add(participant);
		}
	}

    	public ActivityDetail(Activity activity, List<Image> images, List<BasicUser> appliedParticipants, List<BasicUser> selectedParticipants, List<BasicUser> presentParticipants){
		m_id=activity.getId();
		m_title=activity.getTitle();
		m_content=activity.getContent();
		m_createdTime=activity.getCreatedTime();
		m_beginTime=activity.getBeginTime();
		m_deadline=activity.getDeadline();
		m_capacity=activity.getCapacity();
		m_status=activity.getStatus();
		m_hostId=activity.getHostId();	
		m_images=images;
		m_appliedParticipants=appliedParticipants;
		m_selectedParticipants=selectedParticipants;
		m_presentParticipants=presentParticipants;
    	}

	public ActivityDetail(JSONObject activityJson, List<Image> images,
		List<BasicUser> appliedParticipants, List<BasicUser> selectedParticipants, List<BasicUser> presentParticipants) {
		super(activityJson);
		m_images=images;
		m_appliedParticipants=appliedParticipants;
		m_selectedParticipants=selectedParticipants;
		m_presentParticipants=presentParticipants;
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
				for(BasicUser participant : m_appliedParticipants){
					appliedParticipantsNode.add(participant.toObjectNode(viewerId));
				}
				ret.put(ActivityDetail.APPLIED_PARTICIPANTS, appliedParticipantsNode);
			}
			
			if(m_selectedParticipants!=null && m_selectedParticipants.size()>0){
				ArrayNode  selectedParticipantsNode=new ArrayNode(JsonNodeFactory.instance);
				for(BasicUser participant : m_selectedParticipants){
					selectedParticipantsNode.add(participant.toObjectNode(viewerId));
				}
				ret.put(ActivityDetail.SELECTED_PARTICIPANTS, selectedParticipantsNode);
			}
		
			if(m_presentParticipants!=null && m_presentParticipants.size()>0){
				ArrayNode presentParticipantsNode=new ArrayNode(JsonNodeFactory.instance);
				for(BasicUser participant : m_presentParticipants){
					presentParticipantsNode.add(participant.toObjectNode(viewerId));
				}
				ret.put(ActivityDetail.PRESENT_PARTICIPANTS, presentParticipantsNode);
			}

			User user=SQLCommander.queryUser(m_hostId);
			if(user!=null){
				ret.put(HOST_ID, String.valueOf(m_hostId));
				ret.put(HOST_NAME, user.getName());
			}
		} catch (Exception e) {
			System.out.println("ActivityDetail.toObjectNode, "+e.getMessage());
		}
		return ret;
	}
}
