package com.lmn.Arbiter_Android.Dialog.Dialogs;

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

public class FeatureDialogBuilder {
	private Activity activity;
	private View view;
	private Feature feature;
	private LayoutInflater inflater;
	
	public FeatureDialogBuilder(Activity activity, View view, Feature feature){
		this.activity = activity;
		this.view = view;
		this.feature = feature;
		inflater = activity.getLayoutInflater();
	}
	
	public void build(){
		String geometryName = feature.getGeometryName();
		
		String value = null;
		
		ContentValues attributes = feature.getAttributes();
		
		for(String key : attributes.keySet()){
			value = attributes.getAsString(key);
			
			Log.w("FeatureDialogBuilder", "FeatureDialogBuilder: key = " + key + ", value = " + value);
			//if(key.equals(geometryName)){
		//		appendGeometry(value);
		//	}else{
				appendAttribute(key, value);
	//		}
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
	}
}
