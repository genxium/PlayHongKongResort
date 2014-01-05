package controllers;

import java.io.File;

import play.Play;
import play.mvc.Http.MultipartFormData.FilePart;
import model.Activity;
import model.BasicUser;
import model.Image;

public class ExtraCommander extends SQLCommander {
	
	public static boolean deleteImageRecordAndFileByImageId(int imageId){
		boolean ret=false;
		do{
			try{
				Image image=queryImageByImageId(imageId);
				String imageAbsolutePath=image.getImageAbsolutePath();
        			File imageFile=new File(imageAbsolutePath);
        			boolean isFileDeleted=imageFile.delete();
        			boolean isRecordDeleted=deleteImageByImageId(imageId);
        			if(isFileDeleted==false || isRecordDeleted==false) break;
            		ret=true;
			} catch (Exception e){
				System.out.println("ExtraCommander.deleteImageRecordAndFileByImageId: "+e.getMessage());
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
      	    		
      	    		// Save renamed file to server storage at the final step
      	    		boolean renamingResult=file.renameTo(new File(imageAbsolutePath));
      	    		if(renamingResult==false){
      	    			System.out.println("Application.saveAvatarFile: "+newImageName+" could not be saved.");
      	    			// recover table `Image` and `ActivityImageRelationTable`
      	    			boolean isRecovered=SQLCommander.deleteImageByImageId(imageId);
      	    			if(isRecovered==true){
      	    				System.out.println("Application.saveAvatarFile: "+newImageName+" has been reverted");
      	    			}
      	    			break;
      	        }

    				ret=imageId;
            } catch (Exception e) {
                System.out.println("Application.saveAvatarFile: "+e.getMessage());
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
  	    		
  	    		// Save renamed file to server storage at the final step
  	    		boolean renamingResult=file.renameTo(new File(imageAbsolutePath));
  	    		
            if(renamingResult==false){
                System.out.println("Application.saveImageOfActivity: "+newImageName+" could not be saved.");
                // recover table `Image` and `ActivityImageRelationTable`
                boolean isRecovered=SQLCommander.deleteImageByImageId(imageId);
                if(isRecovered==true){
                		System.out.println("Application.saveImageOfActivity: "+newImageName+" has been reverted");
                }
                break;
            }

            ret=imageId;
        } catch (Exception e) {
            System.out.println("Application.saveImageOfActivity: "+e.getMessage());
        }

	}while(false);
	return ret;
}
}
