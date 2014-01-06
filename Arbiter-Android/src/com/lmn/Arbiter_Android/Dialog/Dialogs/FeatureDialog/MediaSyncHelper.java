package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class MediaSyncHelper {
	public static final String MEDIA_TO_SEND = "mediaToSend";
	
	private Activity activity;
	private Context context;
	
	public MediaSyncHelper(Activity activity){
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}
	
	private SQLiteDatabase getDb(){
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(activity);
		
		String path = ProjectStructure.getProjectPath(context, projectName);
		
		return ProjectDatabaseHelper.getHelper(context,
				path, false).getWritableDatabase();
	}
	
	public String getMediaToSend(){
		SQLiteDatabase db = getDb();
		
		String mediaToSend = PreferencesHelper.getHelper().get(db,
				context, MEDIA_TO_SEND);
		
		return mediaToSend;
	}
	
	public void updateMediaToSend(String mediaToSend, boolean insert){
		SQLiteDatabase db = getDb();
		
		if(insert){
			PreferencesHelper.getHelper().insert(db,
					context, MEDIA_TO_SEND, mediaToSend);
		}else{
			PreferencesHelper.getHelper().update(db,
					context, MEDIA_TO_SEND, mediaToSend);
		}
	}
	
	public void clearMediaToSend(){
		SQLiteDatabase db = getDb();
		boolean insert = false;
		
		String mediaToSend = PreferencesHelper.getHelper().get(db,
				context, MEDIA_TO_SEND);
		
		if(mediaToSend == null){
			insert = true;
			mediaToSend = "[]";
		}
		
		if(insert){
			PreferencesHelper.getHelper().insert(db, context,
					MEDIA_TO_SEND, mediaToSend);
		}else{
			PreferencesHelper.getHelper().update(db, context,
					MEDIA_TO_SEND, mediaToSend);
		}
	}
}
