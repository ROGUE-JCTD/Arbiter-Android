package com.lmn.Arbiter_Android.Dialog.ProgressDialog;

import com.lmn.Arbiter_Android.R;

import android.app.Activity;
import android.app.ProgressDialog;


public class PictureProgressDialog {
	
	private static ProgressDialog pictureProgressDialog = null;
	
	public static void show(final Activity activity) {
		
		activity.runOnUiThread(new Runnable(){
			
			@Override
			public void run(){
				
				String title = activity.getResources().getString(R.string.loading);
				String message = activity.getResources().getString(R.string.please_wait);
				
				pictureProgressDialog = ProgressDialog.show(activity, title, message);
			}
		});
	}
	
	public static void dismiss(final Activity activity) {
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(pictureProgressDialog != null) {
					pictureProgressDialog.dismiss();
					pictureProgressDialog = null;
				}
			}
		});
	}
}
