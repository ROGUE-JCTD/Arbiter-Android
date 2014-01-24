package com.lmn.Arbiter_Android.Dialog.Dialogs;

import org.apache.cordova.CordovaInterface;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FailedSync;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog.MediaSyncHelper;

public class FailedSyncHelper {

	private FragmentActivity activity;
	private SQLiteDatabase projectDb;
	private CordovaInterface threadPoolSupplier;
	private ProgressDialog checkForFailedSync;
	
	public FailedSyncHelper(FragmentActivity activity, SQLiteDatabase projectDb){
		this.activity = activity;
		
		this.projectDb = projectDb;
		
		try{
			
			this.threadPoolSupplier = (CordovaInterface) activity;
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}

	public void checkIncompleteSync(){
		
		String title = activity.getResources().getString(R.string.loading);
		
		String message = activity.getResources().getString(R.string.please_wait);
		
		checkForFailedSync = ProgressDialog.show(activity, title, message, true);
		
		threadPoolSupplier.getThreadPool().execute(new Runnable(){
			@Override
			public void run(){
				
				final String[] failedVectorUploads = getFailedVectorUploads();
				
				final String[] failedVectorDownloads = getFailedVectorDownloads();
				
				final JSONObject failedMediaUploads = getFailedMediaUploads();
				
				final String[] failedMediaDownloads = getFailedMediaDownloads();
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						
						if(failedVectorUploads != null){
							Log.w("FailedSyncHelper", "FailedSyncHelper failedVectorUploadsLength = " + failedVectorUploads.length);
						}
						
						if(failedMediaUploads != null){
							Log.w("FailedSyncHelper", "FailedSyncHelper failedMediaUploadsLength = " + failedMediaUploads.length());
						}
						
						if(failedVectorUploads != null){
							Log.w("FailedSyncHelper", "FailedSyncHelper failedVectorDownloadsLength = " + failedVectorDownloads.length);
						}
						
						if(failedMediaDownloads != null){
							
							Log.w("FailedSyncHelper", "FailedSyncHelper failedMediaDownloadsLength = " + failedMediaDownloads.length);
						}
						
						if(isFailed(failedVectorUploads) || isFailed(failedVectorDownloads)
								|| isFailed(failedMediaUploads) || isFailed(failedMediaDownloads)){
							
							Log.w("FailedSyncHelper", "there some failed data!");
							FailedSyncDialog dialog = FailedSyncDialog.newInstance(failedVectorUploads, failedVectorDownloads,
									failedMediaUploads, failedMediaDownloads);
							
							dialog.show(activity.getSupportFragmentManager(), FailedSyncDialog.TAG);
						}else{
							Log.w("FailedSyncHelper", "no failed sync data");
						}
						
						dismiss();
					}
				});
			}
		});
	}
	
	public void dismiss(){
		if(this.checkForFailedSync != null){
			checkForFailedSync.dismiss();
			checkForFailedSync = null;
		}
	}
	
	private boolean isFailed(String[] failed){
		if(failed != null && failed.length > 0){
			return true;
		}
		
		return false;
	}
	
	private boolean isFailed(JSONObject failed){
		
		if(failed != null && failed.length() > 0){
			return true;
		}
		
		return false;
	}
	
	private String[] getFailedVectorUploads(){
		
		return FailedSync.getHelper().getFailedVectorUploads(projectDb);
	}
	
	private String[] getFailedVectorDownloads(){
		
		return FailedSync.getHelper().getFailedVectorDownloads(projectDb);
	}
	
	private JSONObject getFailedMediaUploads(){
		
		String mediaToSendStr = PreferencesHelper.getHelper().get(projectDb,
				activity.getApplicationContext(), MediaSyncHelper.MEDIA_TO_SEND);
		
		Log.w("FailedSyncHelper", "FailedSyncHelper mediaToSendStr - " + mediaToSendStr);
		JSONObject mediaToSend = null;
		
		try {
			if(mediaToSendStr != null){
				mediaToSend = new JSONObject(mediaToSendStr);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mediaToSend;
	}
	
	private String[] getFailedMediaDownloads(){
		
		return FailedSync.getHelper().getFailedMediaDownloads(projectDb);
	}
}
