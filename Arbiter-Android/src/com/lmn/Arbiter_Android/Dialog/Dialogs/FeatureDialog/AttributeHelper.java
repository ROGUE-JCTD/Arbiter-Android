package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.Media.MediaHelper;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;

public class AttributeHelper {
	private HashMap<String, Attribute> attributes;
	
	private Feature feature;
	private ValidityChecker validityChecker;
	
	public AttributeHelper(Feature feature){
		this.attributes = new HashMap<String, Attribute>();
		this.validityChecker = new ValidityChecker();
		this.feature = feature;
	}
	
	public void add(FragmentActivity activity, String key, EditText editText,
			EnumerationHelper enumHelper, boolean startInEditMode, String value){
		
		Attribute attribute = new Attribute(activity, editText,
				enumHelper, startInEditMode, value);
		
		attributes.put(key, attribute);
		
		validityChecker.add(attribute, editText);
	}
	
	public void add(FragmentActivity activity, String key, Spinner spinner,
			EnumerationHelper enumHelper, boolean startInEditMode){
		
		Attribute attribute = new Attribute(activity, spinner,
				enumHelper, startInEditMode);
		
		attributes.put(key, attribute);
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
		
		return validityChecker.checkFormValidity();
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
