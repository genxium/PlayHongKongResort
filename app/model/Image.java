/*
 * This is the base class of image model
 * */

package model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import org.json.simple.JSONObject;
import org.apache.commons.io.FilenameUtils;

public class Image {

	public static String URL_PREFIX="/images/";	
    public static String FOLDER_PATH="/var/www/html/images/";
	public static String idKey="ImageId";
	public static String urlKey="ImageURL";
	
	private int m_imageId=0;
	public int getImageId() {return m_imageId;}
	public void setImageId(int imageId) {m_imageId=imageId;}

	private String m_imageURL=null;
	public String getImageURL() {return m_imageURL;}
	public void setImageURL(String imageURL) {m_imageURL=imageURL;}

    public String getAbsolutePath(){
        String baseName=FilenameUtils.getBaseName(m_imageURL);
        String extension=FilenameUtils.getExtension(m_imageURL);
        return FOLDER_PATH+baseName+"."+extension;
    }

	public Image(int imageId, String imageURL){
		m_imageId=imageId;
		m_imageURL=imageURL;
	}
    
    public Image(JSONObject imageJson){
        try{
            if(imageJson.containsKey(idKey)){
                m_imageId=(Integer)imageJson.get(Image.idKey);
            }
            m_imageURL=(String)imageJson.get(Image.urlKey);
        } catch(Exception e){
             
        }
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
