package com.lmn.Arbiter_Android.CordovaPlugins.Helpers;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class FeatureHelper {
	private FragmentActivity activity;
	private String featureType;
	private String id;
	
	public FeatureHelper(FragmentActivity activity, String featureType, String id){
		this.activity = activity;
		this.featureType = featureType;
		this.id = id;
	}
	
	public void displayFeatureDialog(){
		SQLiteDatabase db = getFeatureDatabase();
		
		Feature feature = getFeature(db, featureType, id);
		
		displayDialog(feature);
	}
	
	private SQLiteDatabase getFeatureDatabase(){
		Context context = activity.getApplicationContext();
		
		String openProjectName = ArbiterProject.
				getArbiterProject().getOpenProject(activity);
		
		return FeatureDatabaseHelper.getHelper(context,
				ProjectStructure.getProjectPath(context, 
						openProjectName)).getWritableDatabase();
	}
	
	private Feature getFeature(SQLiteDatabase db, String featureType, String id){
		return FeaturesHelper.getHelper().
				getFeature(db, id, featureType);
	}
	
	private void displayDialog(Feature feature){
		Resources resources = activity.getResources();
		
		String title = resources.getString(R.string.feature_dialog_title);
		String ok = resources.getString(android.R.string.ok);
		String cancel = resources.getString(android.R.string.cancel);
		
		FeatureDialog dialog = FeatureDialog.newInstance(title, 
				ok, cancel, R.layout.feature_dialog, feature);
		
		FragmentManager manager = activity.getSupportFragmentManager();
		
		dialog.show(manager, "featureDialog");
	}
}
