package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class MediaSyncHelper {
	public static final String MEDIA_TO_SEND = "mediaToSend";
	public static final String LAYERS = "layers";
	
	private Activity activity;
	private Context context;
	
	public MediaSyncHelper(Activity activity){
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}
	
	private SQLiteDatabase getDb(){
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(activity);
		
		String path = ProjectStructure.getProjectPath(projectName);
		
		return ProjectDatabaseHelper.getHelper(context,
				path, false).getWritableDatabase();
	}
	
	public String getMediaToSend(){
		SQLiteDatabase db = getDb();
		
		String mediaToSend = PreferencesHelper.getHelper().get(db,
				context, MEDIA_TO_SEND);
		
		Log.w("MediaSyncHelper", "MediaSyncHelper.getMediaToSend() - " + mediaToSend);
		return mediaToSend;
	}
	
	public void updateMediaToSend(String mediaToSend){
		SQLiteDatabase db = getDb();
		
		PreferencesHelper.getHelper().put(db,
			context, MEDIA_TO_SEND, mediaToSend);
	}
	
	public void clearMediaToSend(){
		SQLiteDatabase db = getDb();
		
		String mediaToSend = "{}";
		
		PreferencesHelper.getHelper().put(db, context,
				MEDIA_TO_SEND, mediaToSend);
	}
}
