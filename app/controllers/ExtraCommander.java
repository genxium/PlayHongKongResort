package controllers;

import java.io.File;

import play.Play;
import play.mvc.Http.MultipartFormData.FilePart;
import model.Activity;
import model.BasicUser;
import model.Image;
import org.apache.commons.io.*;

import model.UserActivityRelation;
import model.UserActivityRelationTable;

import org.json.simple.JSONObject;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import dao.SQLHelper; 

public class ExtraCommander extends SQLCommander {

  public static boolean deleteActivity(int userId, int activityId){
      boolean ret=false;
      do{
        
        try{
          SQLHelper sqlHelper=new SQLHelper();
          String relationTableName="UserActivityRelationTable";
          List<String> relationWhereClauses=new LinkedList<String>();
          relationWhereClauses.add(BasicUser.idKey+"="+SQLHelper.convertToQueryValue(userId));
          relationWhereClauses.add(Activity.idKey+"="+SQLHelper.convertToQueryValue(activityId));
          boolean resultRelationDeletion=sqlHelper.deleteFromTableByWhereClauses(relationTableName, relationWhereClauses, SQLHelper.logicAND);
        
          if(resultRelationDeletion==false) break;
        
          List<Image> previousImages=queryImagesByActivityId(activityId);
          if(previousImages!=null && previousImages.size()>0){
              // delete previous images
              Iterator<Image> itPreviousImage=previousImages.iterator();
              while(itPreviousImage.hasNext()){
                  Image previousImage=itPreviousImage.next();
                  boolean isDeleted=deleteImageRecordAndFileOfActivity(previousImage, activityId);
                  if(isDeleted==false) break;
              }
          }
              
          String activityTableName="Activity";   
          List<String> activityWhereClauses=new LinkedList<String>();
          activityWhereClauses.add(Activity.idKey+"="+SQLHelper.convertToQueryValue(activityId));
          ret=sqlHelper.deleteFromTableByWhereClauses(activityTableName, activityWhereClauses, SQLHelper.logicAND);
        
        } catch(Exception e){
          System.out.println("ExtraCommander.deleteActivity:"+e.getMessage());
        }
      }while(false);

      return ret;
  }

  public static boolean deleteImageRecordAndFile(Image image){
      boolean ret=false;
      do{
        try{
            if(image==null) break;
            int imageId=image.getImageId();
            String imageAbsolutePath=image.getImageAbsolutePath();
            File imageFile=new File(imageAbsolutePath);
            boolean isFileDeleted=imageFile.delete();
            boolean isRecordDeleted=deleteImageRecordById(imageId);
            if(isFileDeleted==false || isRecordDeleted==false) break;
            ret=true;
        } catch (Exception e){
          System.out.println("ExtraCommander.deleteImageRecordAndFile: "+e.getMessage());
        }
      }while(false);
      return ret;
  }

  public static boolean deleteImageRecordAndFileOfActivity(Image image, int activityId){
      boolean ret=false;
      do{
        try{
            if(image==null) break;
            int imageId=image.getImageId();
            String imageAbsolutePath=image.getImageAbsolutePath();
            File imageFile=new File(imageAbsolutePath);
            boolean isFileDeleted=imageFile.delete();
            boolean isRecordDeleted=deleteImageRecordOfActivityById(imageId, activityId);
            if(isFileDeleted==false || isRecordDeleted==false) break;
            ret=true;
        } catch (Exception e){
            System.out.println("ExtraCommander.deleteImageRecordAndFileOfActivity: "+e.getMessage());
        }
      }while(false);
      return ret;
  }
	
	public static int saveAvatarFile(FilePart imageFile, BasicUser user){
		int ret=invalidId;
		do{
			String fileName = imageFile.getFilename();
			File file = imageFile.getFile();
			try {
        			if(DataUtils.validateImage(imageFile)==false) break;
      	    		if(user==null) break;

      	    		Integer userId=user.getUserId();
                String urlFolderName="assets/images";
      	    		String newImageName=DataUtils.generateUploadedImageName(fileName, userId);
                String imageURL="/"+urlFolderName+"/"+newImageName;

                String rootDir=Play.application().path().getAbsolutePath();
                String absoluteFolderName="public/images";
                String imageAbsolutePath=rootDir+"/"+absoluteFolderName+"/"+newImageName;

      	    		int imageId=SQLCommander.uploadUserAvatar(user, imageAbsolutePath, imageURL);
      	    		if(imageId==SQLCommander.invalidId) break;
      	    		
      	    		try{
      	    			// Save renamed file to server storage at the final step
      	    			FileUtils.moveFile(file, new File(imageAbsolutePath));
      	    		} catch(Exception err){
      	    			System.out.println("ExtraCommander.saveAvatarFile: "+newImageName+" could not be saved.");
      	    			// recover table `Image` and `ActivityImageRelationTable`
      	    			boolean isRecovered=SQLCommander.deleteImageRecordById(imageId);
      	    			if(isRecovered==true){
      	    				System.out.println("ExtraCommander.saveAvatarFile: "+newImageName+" has been reverted");
      	    			}
      	    			break;
      	    		}
    				ret=imageId;
            } catch (Exception e) {
                System.out.println("ExtraCommander.saveAvatarFile: "+e.getMessage());
            }

		}while(false);
		return ret;
  }

  public static int saveImageOfActivity(FilePart imageFile, BasicUser user, Activity activity){
  	int ret=invalidId;
  	do{
  		String fileName = imageFile.getFilename();
  		File file = imageFile.getFile();
  		try {
      			if(DataUtils.validateImage(imageFile)==false) break;
    	    		if(user==null) break;
    	    		if(activity==null) break;
    	    		
    	    		Integer userId=user.getUserId();

              String urlFolderName="assets/images";
    	    		String newImageName=DataUtils.generateUploadedImageName(fileName, userId);
              String imageURL="/"+urlFolderName+"/"+newImageName;

              String rootDir=Play.application().path().getAbsolutePath();
              String absoluteFolderName="public/images";
              String imageAbsolutePath=rootDir+"/"+absoluteFolderName+"/"+newImageName;

    	    		int imageId=SQLCommander.uploadImageOfActivity(user, activity, imageAbsolutePath, imageURL);
    	    		if(imageId==SQLCommander.invalidId) break;
    	    		
    	    		try{
                  // Save renamed file to server storage at the final step
                  FileUtils.moveFile(file, new File(imageAbsolutePath));
              } catch(Exception err){
                  System.out.println("ExtraCommander.saveImageOfActivity: "+newImageName+" could not be saved.");
                  // recover table `Image` and `ActivityImageRelationTable`
                  boolean isRecovered=SQLCommander.deleteImageRecordById(imageId);
                  if(isRecovered==true){
                    System.out.println("ExtraCommander.saveImageOfActivity: "+newImageName+" has been reverted");
                  }
                  break;
              }

              ret=imageId;
          } catch (Exception e) {
              System.out.println("ExtraCommander.saveImageOfActivity: "+e.getMessage());
          }

  	}while(false);
  	return ret;
  }
}
