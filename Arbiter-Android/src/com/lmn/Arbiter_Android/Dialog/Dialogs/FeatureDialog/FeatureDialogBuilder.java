package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;

public class FeatureDialogBuilder {
	private Activity activity;
	private View view;
	private Feature feature;
	private LayoutInflater inflater;
	private ArrayList<EditText> editTexts;
	
	public FeatureDialogBuilder(Activity activity, View view, Feature feature){
		this.activity = activity;
		this.view = view;
		this.feature = feature;
		this.editTexts = new ArrayList<EditText>();
		
		inflater = activity.getLayoutInflater();
	}
	
	public void build(){
		String geometryName = feature.getGeometryName();
		
		String value = null;
		
		ContentValues attributes = feature.getAttributes();
		
		for(String key : attributes.keySet()){
			value = attributes.getAsString(key);
			
			if(!key.equals(FeaturesHelper.SYNC_STATE) 
					&& !key.equals(FeaturesHelper.MODIFIED_STATE)){
				
				appendAttribute(key, value);
			}
		}
	}
	
	private void appendGeometry(String geometry){
		
		
	}
	
	private void appendAttribute(String key, String value){
		LinearLayout outer = (LinearLayout) view.findViewById(R.id.outerLayout);
		
		View attributeView = inflater.inflate(R.layout.feature_attribute, null);
		
		if(key != null){
			TextView attributeLabel = (TextView) attributeView.findViewById(R.id.attributeLabel);
			
			if(attributeLabel != null){
				attributeLabel.setText(key);
			}
		}
		
		EditText attributeValue = (EditText) attributeView.findViewById(R.id.attributeText);
		
		if(attributeValue != null){
			attributeValue.setText(value);
		}
		
		outer.addView(attributeView);
		
		editTexts.add(attributeValue);
	}
	
	public boolean toggleEditTexts(){
		EditText editText = null;
		boolean focusable = false;
		
		for(int i = 0, count = editTexts.size(); i < count; i++){
			editText = editTexts.get(i);
			focusable = editText.isFocusable();
			
			editText.setFocusable(!focusable);
			editText.setFocusableInTouchMode(!focusable);
		}
		
		return !focusable;
	}
	
	public void updateFeature(){
		ContentValues attributes = feature.getAttributes();
		int i = 0;
		
		for(String key : attributes.keySet()){
			if(!key.equals(FeaturesHelper.SYNC_STATE) 
					&& !key.equals(FeaturesHelper.MODIFIED_STATE)){
				
				attributes.put(key, editTexts.get(i++)
					.getText().toString());
			}
		}
	}
}
