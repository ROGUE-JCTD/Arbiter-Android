package com.lmn.Arbiter_Android.BaseClasses;

import java.util.LinkedHashMap;

import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;

import android.content.ContentValues;
import android.util.Log;

public class Feature {
	private String id;
	private String featureType;
	private String geometryName;
	private String originalGeometry;
	private LinkedHashMap<String, String> attributes;
	
	public Feature(String id, String featureType, 
			String geometryName, LinkedHashMap<String, String> attributes){
		
		this.id = id;
		this.featureType = featureType;
		this.geometryName = geometryName;
		this.attributes = attributes;
		this.originalGeometry = attributes.get(geometryName);
		
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
	
	public Feature(String featureType, String geometryName, LinkedHashMap<String, String> attributes){
		this.featureType = featureType;
		this.geometryName = geometryName;
		this.attributes = attributes;
				
		this.id = null;
	}
	
	public boolean isNew(){
		return this.id == null || this.id.equals("null");
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
	
	public LinkedHashMap<String, String> getAttributes(){
		return this.attributes;
	}
	
	public void setAttributes(LinkedHashMap<String, String> attributes){
		this.attributes = attributes;
	}
	
	public void updateAttribute(String key, String value){
		attributes.put(key, value);
	}
	
	public String getSyncState(){
		return attributes.get(FeaturesHelper.SYNC_STATE);
	}
	
	public String getFID(){
		return attributes.get(FeaturesHelper.FID);
	}
	
	public void setSyncState(String state){
		attributes.put(FeaturesHelper.SYNC_STATE, state);;
	}
	
	public String getModifiedState(){
		return attributes.get(FeaturesHelper.MODIFIED_STATE);
	}
	
	public void setModifiedState(String state){
		attributes.put(FeaturesHelper.MODIFIED_STATE, state);
	}
	
	public String getOriginalGeometry(){
		return this.originalGeometry;
	}
	
	public String getGeometry(){
		return attributes.get(geometryName);
	}
	
	public void backupGeometry(){
		this.originalGeometry = attributes.get(geometryName);
	}
	
	public void restoreGeometry(){
		attributes.put(geometryName, originalGeometry);
	}
}
