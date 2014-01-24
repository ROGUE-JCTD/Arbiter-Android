package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lmn.Arbiter_Android.R;

import android.app.Activity;
import android.widget.ArrayAdapter;

public class EnumerationHelper {
	public static final String TYPE = "type";
	public static final String RESTRICTION = "enumeration";
	
	private String type;
	private JSONArray restriction;
	private Activity activity;
	
	public EnumerationHelper(Activity activity, JSONObject enumeration){
		
		try{
			this.type = getType(enumeration);
			this.restriction = getRestriction(enumeration);
		} catch(JSONException e){
			e.printStackTrace();
		}
		
		this.activity = activity;
	}
	
	private String getType(JSONObject enumeration) throws JSONException{
		String type = null;
		
		if(enumeration.has(TYPE)){
			type = enumeration.getString(TYPE);
		}
		
		return type;
	}
	
	private JSONArray getRestriction(JSONObject enumeration) throws JSONException{
		JSONArray restriction = null;
		
		if(enumeration.has(RESTRICTION)){
			restriction = enumeration.getJSONArray(RESTRICTION);
		}
		
		return restriction;
	}
	
	public String getType(){
		return this.type;
	}
	
	public boolean hasEnumeration(){
		return (restriction != null) ? true : false;
	}
	
	public ArrayAdapter<String> getSpinnerAdapter() throws JSONException{
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity
				.getApplicationContext(), R.layout.textview);
		
		adapter.add("");
		for(int i = 0, count = restriction.length(); i < count; i++){
			adapter.add(restriction.getString(i));
		}
		
		return adapter;
	}
}
