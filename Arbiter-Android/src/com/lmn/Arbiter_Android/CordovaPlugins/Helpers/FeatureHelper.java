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
	
	public void displayFeatureDialog(Feature feature, String layerId, boolean geomEdited, boolean isReadOnly){
		
		displayDialog(feature, layerId, geomEdited || feature.isNew(), isReadOnly);
	}
	
	private void displayDialog(Feature feature, String layerId, boolean startInEditMode, boolean isReadOnly){
		
		String title = feature.getFeatureType();
		
		FeatureDialog dialog = FeatureDialog.newInstance(title, 
				R.layout.feature_dialog, feature,
				layerId, startInEditMode, isReadOnly);
		
		FragmentManager manager = activity.getSupportFragmentManager();
		
		if(dialog != null) {
			dialog.show(manager, FeatureDialog.TAG);
		}
	}
}
