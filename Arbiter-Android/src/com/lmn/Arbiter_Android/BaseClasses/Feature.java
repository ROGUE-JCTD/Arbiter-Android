package com.lmn.Arbiter_Android.BaseClasses;

import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;

import android.content.ContentValues;
import android.util.Log;

public class Feature {
	private String id;
	private String featureType;
	private String geometryName;
	private String originalGeometry;
	private ContentValues attributes;
	
	public Feature(String id, String featureType, 
			String geometryName, ContentValues attributes){
		
		this.id = id;
		this.featureType = featureType;
		this.geometryName = geometryName;
		this.attributes = attributes;
		
		if(getSyncState() == null){
			try {
				throw new Exception("Feature(): syncState should not be null");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(getModifiedState() == null){
			try {
				throw new Exception("Feature(): modifiedState should not be null");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Feature(String featureType, String geometryName, ContentValues attributes){
		this.featureType = featureType;
		this.geometryName = geometryName;
		this.attributes = attributes;
				
		this.id = null;
	}
	
	public boolean isNew(){
		return this.id == null;
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getFeatureType(){
		return this.featureType;
	}
	
	public String getGeometryName(){
		return this.geometryName;
	}
	
	public ContentValues getAttributes(){
		return this.attributes;
	}
	
	public void setAttributes(ContentValues attributes){
		this.attributes = attributes;
	}
	
	public void updateAttribute(String key, String value){
		attributes.put(key, value);
	}
	
	public String getSyncState(){
		return attributes.getAsString(FeaturesHelper.SYNC_STATE);
	}
	
	public void setSyncState(String state){
		attributes.put(FeaturesHelper.SYNC_STATE, state);;
	}
	
	public String getModifiedState(){
		return attributes.getAsString(FeaturesHelper.MODIFIED_STATE);
	}
	
	public void setModifiedState(String state){
		attributes.put(FeaturesHelper.MODIFIED_STATE, state);
	}
	
	public String getOriginalGeometry(){
		return this.originalGeometry;
	}
	
	public String getGeometry(){
		return attributes.getAsString(geometryName);
	}
	
	public void backupGeometry(){
		this.originalGeometry = attributes.getAsString(geometryName);
	}
	
	public void restoreGeometry(){
		attributes.put(geometryName, originalGeometry);
	}
	
	public boolean geometryChanged(){
		boolean geometryChanged = getGeometry().equals(originalGeometry);
		
		Log.w("Feature", "Feature - originalGeometry = " 
				+ originalGeometry + ", new = " + getGeometry() 
				+ ", geometryChanged = " + geometryChanged);
		return geometryChanged;
	}
}
