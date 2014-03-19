/*
 * This is the base class of image model
 * */

package model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

public class Image {
	
	public static String idKey="ImageId";
	public static String absolutePathKey="ImageAbsolutePath";
	public static String urlKey="ImageURL";
	
	private int m_imageId=0;
	public int getImageId() {return m_imageId;}
	public void setImageId(int imageId) {m_imageId=imageId;}

	private String m_imageAbsolutePath=null;
	public String getImageAbsolutePath() {return m_imageAbsolutePath;}
	public void setImageAbsolutePath(String imageAbsolutePath) {m_imageAbsolutePath=imageAbsolutePath;}
	
	private String m_imageURL=null;
	public String getImageURL() {return m_imageURL;}
	public void setImageURL(String imageURL) {m_imageURL=imageURL;}

	public Image(int imageId, String imageAbsolutePath, String imageURL){
		m_imageId=imageId;
		m_imageAbsolutePath=imageAbsolutePath;
		m_imageURL=imageURL;
	}
	
	public static Image create(int imageId, String imageAbsolutePath, String imageURL){
		Image image=new Image(imageId, imageAbsolutePath, imageURL);
		return image;
	}

    public ObjectNode toObjectNode(){
        ObjectNode ret= Json.newObject();
        do {
            try{
                ret.put(idKey, m_imageId);
                ret.put(urlKey, m_imageURL);
            } catch (Exception e){

            }
        }while(false);
        return ret;
    }
}