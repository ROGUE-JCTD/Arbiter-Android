package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.HashMap;

import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.Media.MediaHelper;

import android.content.ContentValues;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Spinner;

public class AttributeHelper {
	private HashMap<String, Attribute> attributes;
	
	private Feature feature;
	
	private int invalidCount;
	
	public AttributeHelper(Feature feature){
		this.attributes = new HashMap<String, Attribute>();
		
		this.feature = feature;
	}
	
	public void add(FragmentActivity activity, String key, final EditText editText,
			EnumerationHelper enumHelper, boolean startInEditMode, String value){
		
		final Attribute attribute = new Attribute(activity, editText,
				enumHelper, startInEditMode, value);
		
		editText.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				
				checkValidity(attribute);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
		});
		
		attributes.put(key, attribute);
	}
	
	private boolean checkValidity(Attribute attribute){
		boolean startingValidity = attribute.isValid();
		
		boolean updatedValidity = attribute.updateValidity();
		
		if(!updatedValidity && startingValidity){
			invalidCount++;
		}else if(updatedValidity && !startingValidity){
			invalidCount--;
		}
		
		return updatedValidity;
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
			
			if(key.equals(feature.getGeometryName())){
				attribute.setEditMode(false);
			}else{
				attribute.setEditMode(editMode);
			}
		}
		
		return editMode;
	}
	
	public boolean checkFormValidity(){
		return invalidCount == 0;
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
