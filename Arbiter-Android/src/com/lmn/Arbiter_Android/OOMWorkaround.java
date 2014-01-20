package com.lmn.Arbiter_Android;

import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class OOMWorkaround {
	private Activity activity;
	private Context context;
	
	public OOMWorkaround(Activity activity){
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}
	
	// Don't keep a db because it will change when
	// the project gets switched.
	private SQLiteDatabase getProjectDatabase(){
		
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(activity);
		
		String path = ProjectStructure.getProjectPath(projectName);
		
		return ProjectDatabaseHelper.getHelper(context, 
				path, false).getWritableDatabase();
	}
	
	public void resetSavedBounds(boolean isCreatingProject){
		
		SQLiteDatabase db = getProjectDatabase();
			
		PreferencesHelper.getHelper().put(db, context, 
			PreferencesHelper.SHOULD_ZOOM_TO_AOI, 
			Boolean.toString(!isCreatingProject));
		
		PreferencesHelper.getHelper().delete(db, context, PreferencesHelper.SAVED_BOUNDS);
		PreferencesHelper.getHelper().delete(db, context, PreferencesHelper.SAVED_ZOOM_LEVEL);
	}
	
	public void setSavedBounds(String bounds, String zoom, boolean isCreatingProject){
		SQLiteDatabase db = getProjectDatabase();
		
		PreferencesHelper.getHelper().put(db, context, PreferencesHelper.SAVED_BOUNDS, bounds);
		PreferencesHelper.getHelper().put(db, context, PreferencesHelper.SAVED_ZOOM_LEVEL, zoom);
	}
}
