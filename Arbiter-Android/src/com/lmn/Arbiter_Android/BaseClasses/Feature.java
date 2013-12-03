package com.lmn.Arbiter_Android.BaseClasses;

import android.content.ContentValues;

public class Feature {
	private String id;
	private String geometryName;
	private ContentValues attributes;
	
	public Feature(String id, String geometryName, ContentValues attributes){
		this.id = id;
		this.geometryName = geometryName;
		this.attributes = attributes;
	}
	
	public String getId(){
		return this.id;
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
