package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.HashMap;

import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.Media.MediaHelper;

import android.content.ContentValues;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;
import android.widget.Spinner;

public class AttributeHelper {
	private HashMap<String, Attribute> attributes;
	
	private Feature feature;
	
	public AttributeHelper(Feature feature){
		this.attributes = new HashMap<String, Attribute>();
		
		this.feature = feature;
	}
	
	public void add(FragmentActivity activity, String key, EditText editText,
			EnumerationHelper enumHelper, boolean startInEditMode, String value){
		
		attributes.put(key,  new Attribute(activity, editText,
				enumHelper, startInEditMode, value));
	}
	
	public void add(FragmentActivity activity, String key, Spinner spinner,
			EnumerationHelper enumHelper, boolean startInEditMode){
		
		attributes.put(key,  new Attribute(activity, spinner,
				enumHelper, startInEditMode));
	}
	
	public boolean setEditMode(boolean editMode){
		
		Attribute attribute = null;
		
		for(String key : attributes.keySet()){
			attribute = attributes.get(key);
			
			attribute.setEditMode(editMode);
		}
		
		return editMode;
	}
	
	public void updateFeature(){
		ContentValues featureAttributes = feature.getAttributes();
		
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
					featureAttributes.put(key, attribute.getValue());
				}
			}
		}
	}
}
