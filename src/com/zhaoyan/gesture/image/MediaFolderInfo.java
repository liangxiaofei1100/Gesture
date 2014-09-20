package com.zhaoyan.gesture.image;

import java.util.ArrayList;
import java.util.List;


//表示一个图片文件夹和文件夹里面的图片信息
public class MediaFolderInfo {

	/**
	 * image folder name
	 */
	private String bucket_display_name;
	
	private String display_name;
	private String path;
	
	/***
	 * save the image ids that in a same folder
	 */
	private List<Long> idList;
	
	public MediaFolderInfo(){
		idList = new ArrayList<Long>();
	}
	
	public void setBucketDisplayName(String name){
		this.bucket_display_name = name;
	}
	
	public String getBucketDisplayName(){
		return bucket_display_name;
	}
	
	public void setDisplayName(String name){
		this.display_name = name;
	}
	
	public String getDisplayName(){
		return display_name;
	}
	
	public void setImagePath(String path){
		this.path = path;
	}
	
	public String getImagePath(){
		return path;
	}
	
	public void addIdToList(long image_id){
		idList.add(image_id);
	}
	
	public List<Long> getIdList(){
		return idList;
	}
	
	
	/***
	 * get {@link MediaFolderInfo} from {@link mFolderInfosList} accord to the
	 * speciy bucketDisplayName}}
	 * 
	 * @param bucketDisplayName
	 * @return {@link PictureFolderInfo}, null if not find
	 */
	public static MediaFolderInfo getFolderInfo(String bucketDisplayName, List<MediaFolderInfo> list) {
		for (MediaFolderInfo folderInfo : list) {
			if (bucketDisplayName.equals(folderInfo.getBucketDisplayName())) {
				return folderInfo;
			}
		}
		return null;
	}
}
