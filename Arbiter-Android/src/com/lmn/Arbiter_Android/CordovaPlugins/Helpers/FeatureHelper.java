package com.lmn.Arbiter_Android.CordovaPlugins.Helpers;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.ArbiterState;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ControlPanelHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog.FeatureDialog;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class FeatureHelper {
	private FragmentActivity activity;
	
	public FeatureHelper(FragmentActivity activity){
		this.activity = activity;
	}
	
	private Feature getNewFeature(String featureType, String wktGeometry){
		return FeaturesHelper.getHelper().getNewFeature(
				getFeatureDatabase(), featureType, wktGeometry);
	}
	
	public void displayFeatureDialog(String featureType, String featureId,
			String layerId, String wktGeometry, String mode){
		
		Feature feature = null;
		boolean startInEditMode = false;
		
		if(featureId == null || featureId.equals("null")
				|| featureId.equals("undefined")){
			
			feature = ArbiterState.getArbiterState().isEditingFeature();
			
			// Inserting a new feature
			if(feature == null){
				feature = getNewFeature(featureType, wktGeometry);
			}
		}else{  // Existing feature is selected
			
			SQLiteDatabase db = getFeatureDatabase();
			
			feature = getFeature(db, featureType, featureId);
			
			feature.backupGeometry();
		}
			
		Log.w("FeatureHelper", "FeatureHelper displayFeatureDialog mode = " + mode);
		// Update the features geometry
		if(mode.equals(ControlPanelHelper.CONTROLS.INSERT) 
				|| mode.equals(ControlPanelHelper.CONTROLS.MODIFY)){
			
			if(!wktGeometry.equals("null")){
				feature.getAttributes().put(feature
						.getGeometryName(), wktGeometry);
			}
			
			startInEditMode = true;
		}
		
		displayDialog(feature, layerId, startInEditMode);
	}
	
	private SQLiteDatabase getFeatureDatabase(){
		Context context = activity.getApplicationContext();
		
		String openProjectName = ArbiterProject.
				getArbiterProject().getOpenProject(activity);
		
		return FeatureDatabaseHelper.getHelper(context,
				ProjectStructure.getProjectPath(context, 
						openProjectName), false).getWritableDatabase();
	}
	
	private Feature getFeature(SQLiteDatabase db, String featureType, String id){
		return FeaturesHelper.getHelper().
				getFeature(db, id, featureType);
	}
	
	private void displayDialog(Feature feature, String layerId, boolean startInEditMode){
		
		Resources resources = activity.getResources();
		
		String title = resources.getString(R.string.feature_dialog_title);
		
		FeatureDialog dialog = FeatureDialog.newInstance(title, 
				R.layout.feature_dialog, feature, layerId, startInEditMode);
		
		FragmentManager manager = activity.getSupportFragmentManager();
		
		dialog.show(manager, FeatureDialog.TAG);
	}
}
