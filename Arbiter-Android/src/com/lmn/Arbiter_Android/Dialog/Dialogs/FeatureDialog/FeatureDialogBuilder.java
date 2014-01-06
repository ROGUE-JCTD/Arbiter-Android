package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.ArrayList;

import org.json.JSONException;

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
import com.lmn.Arbiter_Android.Media.MediaHelper;

public class FeatureDialogBuilder {
	private Feature feature;
	private LayoutInflater inflater;
	private ArrayList<EditText> editTexts;
	private LinearLayout outerLayout;
	private MediaBuilder mediaBuilder;
	
	public FeatureDialogBuilder(Activity activity, View view, Feature feature){
		this.feature = feature;
		this.editTexts = new ArrayList<EditText>();
		
		this.inflater = activity.getLayoutInflater();
		this.outerLayout = (LinearLayout) view.findViewById(R.id.outerLayout);
		
		this.mediaBuilder = new MediaBuilder(activity, feature,
				this.outerLayout, this.inflater);
		
	}
	
	public void build(){
		String geometryName = feature.getGeometryName();
		
		String value = null;
		
		ContentValues attributes = feature.getAttributes();
		
		for(String key : attributes.keySet()){
			value = attributes.getAsString(key);
			
			if(!key.equals(FeaturesHelper.SYNC_STATE) 
					&& !key.equals(FeaturesHelper.MODIFIED_STATE) 
					&& !key.equals(FeaturesHelper.FID)){
				
				if(key.equals(MediaHelper.MEDIA) || key.equals(MediaHelper.FOTOS)){
					try {
						Log.w("FeatureDialogBuilder", "FeatureDialogBuilder.appendMedia(" + value + ")");
						
						mediaBuilder.appendMedia(key, value);
					} catch (JSONException e) {
						Log.e("FeatureDialogBuilder", "FeatureDialogBuilder.build() could not parse media json");
						e.printStackTrace();
					}
				}else{
					appendAttribute(key, value);
				}
			}
		}
	}
	
	private void appendGeometry(String geometry){
		
		
	}
	
	private void appendAttribute(String key, String value){
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
		
		outerLayout.addView(attributeView);
		
		editTexts.add(attributeValue);
	}
	
	public boolean toggleEditMode(){
		EditText editText = null;
		boolean focusable = false;
		
		for(int i = 0, count = editTexts.size(); i < count; i++){
			editText = editTexts.get(i);
			focusable = editText.isFocusable();
			
			editText.setFocusable(!focusable);
			editText.setFocusableInTouchMode(!focusable);
		}
		
		mediaBuilder.toggleEditMode();
		
		return !focusable;
	}
	
	public void updateFeature(){
		ContentValues attributes = feature.getAttributes();
		int i = 0;
		
		for(String key : attributes.keySet()){
			if(!key.equals(FeaturesHelper.SYNC_STATE) 
					&& !key.equals(FeaturesHelper.MODIFIED_STATE)
					&& !key.equals(FeaturesHelper.FID)
					&& !key.equals(MediaHelper.FOTOS)
					&& !key.equals(MediaHelper.MEDIA)){
				
					attributes.put(key, editTexts.get(i++)
							.getText().toString());
			}
		}
	}
	
	public Feature updateFeaturesMedia(String key, String media){
		ContentValues attributes = feature.getAttributes();
		
		attributes.put(key, media);
		
		return feature;
	}
}
