package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.Media.MediaHelper;

import android.support.v4.app.FragmentActivity;
import android.widget.EditText;
import android.widget.Spinner;

public class AttributeHelper {
	private HashMap<String, Attribute> attributes;
	
	private Feature feature;
	private ValidityChecker validityChecker;
	private Util util;
	
	public AttributeHelper(Feature feature, Util util){
		this.attributes = new HashMap<String, Attribute>();
		this.feature = feature;
		this.util = util;
		this.validityChecker = new ValidityChecker();
	}
	
	public void add(FragmentActivity activity, String key, EditText editText,
			EnumerationHelper enumHelper, boolean isNillable, boolean startInEditMode, String value){
		
		Attribute attribute = new Attribute(activity, editText,
				enumHelper, isNillable, startInEditMode, value, util);
		
		attributes.put(key, attribute);
		
		validityChecker.add(attribute, editText);
	}
	
	public void add(FragmentActivity activity, String key, Spinner spinner, EditText errorEditText,
			EnumerationHelper enumHelper, boolean isNillable, boolean startInEditMode){
		
		Attribute attribute = new Attribute(activity, spinner, errorEditText,
				enumHelper, isNillable, startInEditMode, util);
		
		attributes.put(key, attribute);
		
		validityChecker.add(key, attribute, spinner);
	}
	
	public boolean setEditMode(boolean editMode){
		
		Attribute attribute = null;
		
		for(String key : attributes.keySet()){
			attribute = attributes.get(key);
			
			if(key.equals(feature.getGeometryName())){
				attribute.setEditMode(false);
			}else{
				attribute.setEditMode(editMode);
			}
		}
		
		return editMode;
	}
	
	public boolean checkFormValidity(){
		if(validityChecker == null){
			return true;
		}
		
		boolean valid = true;
		Attribute attribute = null;
		
		for(String key : attributes.keySet()){
			
			attribute = attributes.get(key);
			
			if(!attribute.updateValidity()){
				valid = false;
			}
		}
		
		return valid;
	}
	
	public void updateFeature(){
		LinkedHashMap<String, String> featureAttributes = feature.getAttributes();
		
		Attribute attribute = null;
		String value = null;
		
		for(String key : attributes.keySet()){
			
			if(!key.equals(FeaturesHelper.SYNC_STATE) 
					&& !key.equals(FeaturesHelper.MODIFIED_STATE)
					&& !key.equals(FeaturesHelper.FID)
					&& !key.equals(MediaHelper.FOTOS)
					&& !key.equals(MediaHelper.MEDIA)){
				
				attribute = attributes.get(key);
				
				value = attribute.getValue();
				
				if(value != null){
					featureAttributes.put(key, value);
				}
			}
		}
	}
}
