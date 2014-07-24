package com.lmn.Arbiter_Android.BaseClasses;

import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FailedSync;

public class FailedSyncObj {

	private int id;
	private String key;
	private int dataType;
	private int syncType;
	private int layerId;
	private int errorType;
	private String featureType;
	
	public FailedSyncObj(){
		
		this.id = -1;
		this.key = null;
		this.dataType = -1;
		this.syncType = -1;
		this.layerId = -1;
		this.errorType = -1;
		this.featureType = null;
	}
	
	public FailedSyncObj(int id, String key, int dataType, int syncType, int layerId, int errorType, String featureType){
		
		setId(id);
		setKey(key);
		setDataType(dataType);
		setSyncType(syncType);
		setLayerId(layerId);
		setErrorType(errorType);
		setFeatureType(featureType);
	}
	
	public int getId(){
		return this.id;
	}
	
	public String getKey(){
		return this.key;
	}
	
	public int getDataType(){
		return this.dataType;
	}
	
	public int getSyncType(){
		
		return this.syncType;
	}
	
	public int getLayerId(){
		
		return this.layerId;
	}
	
	public int getErrorType(){
		
		return this.errorType;
	}
	
	public String getFeatureType(){
		return this.featureType;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public void setKey(String key){
		this.key = key;
	}
	
	public void setDataType(int dataType){
		this.dataType = dataType;
	}
	
	public void setSyncType(int syncType){
		this.syncType = syncType;
	}
	
	public void setLayerId(int layerId){
		this.layerId = layerId;
	}
	
	public void setErrorType(int errorType){
		this.errorType = errorType;
	}
	
	public void setFeatureType(String featureType){
		this.featureType = featureType;
	}
	
	public boolean isVector(){
		return dataType == FailedSync.DataType.VECTOR;
	}
	
	public boolean isMedia(){
		return dataType == FailedSync.DataType.MEDIA;
	}
	
	public boolean isUpload(){
		return syncType == FailedSync.SyncType.UPLOAD;
	}
	
	public boolean isDownload(){
		return syncType == FailedSync.SyncType.DOWNLOAD;
	}
}
