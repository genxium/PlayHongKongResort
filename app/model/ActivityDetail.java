package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ActivityDetail extends Activity {

	public static String imagesKey="ActivityImages";
	public static String appliedParticipantsKey="ActivityAppliedParticipants";
	public static String selectedParticipantsKey="ActivitySelectedParticipants";
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

	public ActivityDetail(int id, String title, String content,
			Timestamp createdTime, Timestamp beginTime, Timestamp deadline,
			int capacity, StatusType status, List<Image> images, 
            List<BasicUser> appliedParticipants, List<BasicUser> selectedParticipants) {
		super(id, title, content, createdTime, beginTime, deadline, capacity, status);
		m_images=images;
		m_appliedParticipants=appliedParticipants;
        m_selectedParticipants=selectedParticipants;
	}

	public ActivityDetail(Activity activity, List<Image> images, List<BasicUser> appliedParticipants, List<BasicUser> selectedParticipants){
		super(activity.getId(), activity.getTitle(), activity.getContent(), 
			activity.getCreatedTime(), activity.getBeginTime(), activity.getDeadline(), 
			activity.getCapacity(), activity.getStatus());
		m_images=images;
		m_appliedParticipants=appliedParticipants;
        m_selectedParticipants=selectedParticipants;
	}
	
	public ObjectNode toObjectNode(){
		ObjectNode ret = null;
        do{
        	ret=Json.newObject();
    		
        	ret.put(Activity.idKey, String.valueOf(m_id));
        	ret.put(Activity.titleKey, m_title);
        	ret.put(Activity.contentKey, m_content);
        	ret.put(Activity.statusKey, String.valueOf(m_status));
    			
			if(m_images!=null && m_images.size()>0){
			   ArrayNode imagesNode=new ArrayNode(JsonNodeFactory.instance);
		       Iterator<Image> itImage=m_images.iterator();
		       while(itImage.hasNext()){
		    	  ObjectNode singleImageNode=Json.newObject();
				  Image image=itImage.next();
                  Integer imageId=image.getImageId();
				  String imageURL=image.getImageURL();
				  singleImageNode.put(Image.idKey, imageId);
				  singleImageNode.put(Image.urlKey, imageURL);
				  imagesNode.add(singleImageNode);
		       }
		       ret.put(ActivityDetail.imagesKey, imagesNode);
			}
			
			if(m_appliedParticipants!=null && m_appliedParticipants.size()>0){
				ArrayNode appliedParticipantsNode=new ArrayNode(JsonNodeFactory.instance);
				Iterator<BasicUser> itParticipant=m_appliedParticipants.iterator();
				while(itParticipant.hasNext()){
					ObjectNode singleParticipantNode=Json.newObject();
					BasicUser participant=itParticipant.next();
					singleParticipantNode.put(BasicUser.emailKey, participant.getEmail());
					singleParticipantNode.put(BasicUser.nameKey, participant.getName());
					appliedParticipantsNode.add(singleParticipantNode);
				}
				ret.put(ActivityDetail.appliedParticipantsKey, appliedParticipantsNode);
			}
			
            if(m_selectedParticipants!=null && m_selectedParticipants.size()>0){
				ArrayNode  selectedParticipantsNode=new ArrayNode(JsonNodeFactory.instance);
				Iterator<BasicUser> itParticipant=m_selectedParticipants.iterator();
				while(itParticipant.hasNext()){
					ObjectNode singleParticipantNode=Json.newObject();
					BasicUser participant=itParticipant.next();
					singleParticipantNode.put(BasicUser.emailKey, participant.getEmail());
					singleParticipantNode.put(BasicUser.nameKey, participant.getName());
					selectedParticipantsNode.add(singleParticipantNode);
				}
				ret.put(ActivityDetail.selectedParticipantsKey, selectedParticipantsNode);
			}
        }while(false);
		return ret;
	}
}
