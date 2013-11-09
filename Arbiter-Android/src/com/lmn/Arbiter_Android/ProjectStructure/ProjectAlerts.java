package com.lmn.Arbiter_Android.ProjectStructure;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import com.lmn.Arbiter_Android.R;

public class ProjectAlerts {
	private static final String TAG = "ProjectAlerts";
	
	public ProjectAlerts(){
		
	}
	
	public void alertProjectAlreadyExists(Activity activity){
		Log.w(TAG, TAG + " alertProjectAlreadyExists");
		createAlertDialog(activity, R.string.project_exists_title, 
				R.string.project_already_exists, R.drawable.icon, null, null);
		
	}
	
	public void alertCreateProjectFailed(Activity activity){
		createAlertDialog(activity, R.string.project_creation_failed, 
				R.string.project_creation_failed_message, R.drawable.icon, null, null);
	}
	
	public void createAlertDialog(Activity activity, int title, int message, 
			int icon, final Runnable okCallback, final Runnable cancelCallback){
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(title);
		builder.setIcon(activity.getResources().getDrawable(icon));
		builder.setMessage(message);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(okCallback != null){
					okCallback.run();
				}
			}
		});
		
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(cancelCallback != null){
					cancelCallback.run();
				}
			}
		});
		
		builder.create().show();
	}
}
