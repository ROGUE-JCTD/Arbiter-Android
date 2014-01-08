package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.HashMap;

import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.Media.MediaHelper;

import android.content.ContentValues;
import android.widget.EditText;
import android.widget.Spinner;

public class AttributeHelper {
	private HashMap<String, EditText> editTexts;
	private HashMap<String, Spinner> spinners;
	
	private Feature feature;
	
	public AttributeHelper(Feature feature){
		this.editTexts = new HashMap<String, EditText>();
		this.spinners = new HashMap<String, Spinner>();
		this.feature = feature;
	}
	
	public void add(String key, EditText editText){
		editTexts.put(key, editText);
	}
	
	public void add(String key, Spinner spinner){
		spinners.put(key, spinner);
	}
	
	public boolean toggleEditMode(){
		EditText editText = null;
		boolean focusable = false;
		
		for(String key : editTexts.keySet()){
			editText = editTexts.get(key);
			focusable = editText.isFocusable();
			
			editText.setFocusable(!focusable);
			editText.setFocusableInTouchMode(!focusable);
		}
		
		Spinner spinner = null;
		
		for(String key : spinners.keySet()){
			spinner = spinners.get(key);
			focusable = spinner.isEnabled();
			
			spinner.setEnabled(!focusable);
		}
		
		return !focusable;
	}
	
	public void updateFeature(){
		ContentValues attributes = feature.getAttributes();
		
		for(String key : editTexts.keySet()){
			if(!key.equals(FeaturesHelper.SYNC_STATE) 
					&& !key.equals(FeaturesHelper.MODIFIED_STATE)
					&& !key.equals(FeaturesHelper.FID)
					&& !key.equals(MediaHelper.FOTOS)
					&& !key.equals(MediaHelper.MEDIA)){
				
					attributes.put(key, editTexts.get(key)
							.getText().toString());
			}
		}
		
		String value = null;
		
		for(String key : editTexts.keySet()){
			value = editTexts.get(key).getText().toString();
			
			attributes.put(key, value);
		}
		
		Spinner spinner = null;
		
		for(String key : spinners.keySet()){
			spinner = spinners.get(key); 
			value = (String) spinner.getSelectedItem();
			
			attributes.put(key, value);
		}
	}
}
