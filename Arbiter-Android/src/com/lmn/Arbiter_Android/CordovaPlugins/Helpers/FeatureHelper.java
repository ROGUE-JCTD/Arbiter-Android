package com.lmn.Arbiter_Android.CordovaPlugins.Helpers;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog.FeatureDialog;

public class FeatureHelper {
	private FragmentActivity activity;
	
	public FeatureHelper(FragmentActivity activity){
		this.activity = activity;
	}
	
	public void displayFeatureDialog(Feature feature, String layerId, boolean geomEdited){
		
		displayDialog(feature, layerId, geomEdited || feature.isNew());
	}
	
	private void displayDialog(Feature feature, String layerId, boolean startInEditMode){
		
		String title = feature.getFeatureType();
		
		FeatureDialog dialog = FeatureDialog.newInstance(title, 
				R.layout.feature_dialog, feature, layerId, startInEditMode);
		
		FragmentManager manager = activity.getSupportFragmentManager();
		
		dialog.show(manager, FeatureDialog.TAG);
	}
}
