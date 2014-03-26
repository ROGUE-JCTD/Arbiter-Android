package com.lmn.Arbiter_Android.BaseClasses;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseLayer {
	
	public static final String NAME = "name";
	
	private JSONObject baseLayer;
	
	public BaseLayer(JSONObject baseLayer){
		this.baseLayer = baseLayer;
	}
	
	public String getName() throws JSONException{
		
		return (this.baseLayer != null) ? this.baseLayer.getString(NAME) : "";
	}
}
