package com.lmn.Arbiter_Android.BaseClasses;

import android.content.ContentValues;

public class Feature {
	private String id;
	private String featureType;
	private String geometryName;
	private ContentValues attributes;
	
	public Feature(String id, String featureType, 
			String geometryName, ContentValues attributes){
		
		this.id = id;
		this.featureType = featureType;
		this.geometryName = geometryName;
		this.attributes = attributes;
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
	
	/*public Feature(Feature feature){
		this.id = feature.getId();
		this.geometryName = feature.getGeometryName();
		this.attributes = copyAttributes(feature.getAttributes());
	}
	
	private ContentValues copyAttributes(ContentValues _attributes){
		ContentValues attributes = new ContentValues();
		
		for(String key : _attributes.keySet()){
			attributes.put(key, _attributes.getAsString(key));
		}
		
		return attributes;
	}*/
	
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
}
