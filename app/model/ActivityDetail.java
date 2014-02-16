package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.SQLCommander;

public class ActivityDetail extends Activity {

	public static String imagesKey="ActivityImages";
	public static String participantsKey="ActivityParticipants";
	
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
	
	protected List<BasicUser> m_participants=null;
	public List<BasicUser> getParticipants() {return m_participants;}
	public void setParticipants(List<BasicUser> participants){
		if(m_participants!=null){
			m_participants.clear();
		} else{
			m_participants=new ArrayList<BasicUser>();
		}
		Iterator<BasicUser> it=participants.iterator();
		while(it.hasNext()){
			BasicUser participant=it.next();
			m_participants.add(participant);
		}
	}
	
	public ActivityDetail(int id, String title, String content,
			Timestamp createdTime, Timestamp beginTime, Timestamp deadline,
			int capacity, StatusType status, List<Image> images, List<BasicUser> participants) {
		super(id, title, content, createdTime, beginTime, deadline, capacity, status);
		m_images=images;
		m_participants=participants;
		// TODO Auto-generated constructor stub
	}

	public ActivityDetail(Activity activity, List<Image> images, List<BasicUser> participants){
		super(activity.getId(), activity.getTitle(), activity.getContent(), 
			activity.getCreatedTime(), activity.getBeginTime(), activity.getDeadline(), 
			activity.getCapacity(), activity.getStatus());
		m_images=images;
		m_participants=participants;
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
			   ObjectNode imagesNode=Json.newObject();
		       Iterator<Image> itImage=m_images.iterator();
		       while(itImage.hasNext()){
		    	  ObjectNode singleImageNode=Json.newObject();
				  Image image=itImage.next();
				  String imageURL=image.getImageURL();
				  singleImageNode.put(Image.urlKey, imageURL);
				  imagesNode.put(Image.idKey, singleImageNode);
		       }
		       ret.put(ActivityDetail.imagesKey, imagesNode);
			}
			
			if(m_participants!=null && m_participants.size()>0){
				ObjectNode participantsNode=Json.newObject();
				Iterator<BasicUser> itParticipant=m_participants.iterator();
				while(itParticipant.hasNext()){
					ObjectNode singleParticipantNode=Json.newObject();
					BasicUser participant=itParticipant.next();
					singleParticipantNode.put(BasicUser.emailKey, participant.getEmail());
					singleParticipantNode.put(BasicUser.nameKey, participant.getName());
					participantsNode.put(BasicUser.idKey, singleParticipantNode);
				}
				ret.put(ActivityDetail.participantsKey, participantsNode);
			}
        }while(false);
		return ret;
	}
}
