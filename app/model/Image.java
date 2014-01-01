/*
 * This is the base class of image model
 * */

package model;

import org.json.simple.JSONObject;

import play.libs.Json;
import utilities.Converter;

import com.fasterxml.jackson.databind.node.ObjectNode;

import dao.SQLHelper;

public class Image {
	
	public static String idKey="ImageId";
	public static String urlKey="ImageURL";
	
	private int m_imageId=0;
	public int getImageId() {return m_imageId;}
	public void setImageId(int ImageId) {m_imageId=ImageId;}
	
	private String m_imageURL=null;
	public String getImageURL() {return m_imageURL;}
	public void setImageURL(String imageURL) {m_imageURL=imageURL;}

	public Image(int imageId, String imageURL){
		m_imageId=imageId;
		m_imageURL=imageURL;
	}
	
	public static Image create(int imageId, String imageURL){
		Image image=new Image(imageId, imageURL);
		return image;
	}
}