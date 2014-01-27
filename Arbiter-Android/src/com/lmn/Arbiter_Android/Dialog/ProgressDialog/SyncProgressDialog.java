package com.lmn.Arbiter_Android.Dialog.ProgressDialog;

import com.lmn.Arbiter_Android.R;

import android.app.Activity;
import android.app.ProgressDialog;


public class SyncProgressDialog {
	
	private static ProgressDialog syncProgressDialog = null;
	
	public static void setTitle(final Activity activity, final String title) {
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(syncProgressDialog != null) {
					syncProgressDialog.setTitle(title);
				}
			}
		});
	}
	
	public static void setMessage(final Activity activity, final String message) {
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(syncProgressDialog != null) {
					syncProgressDialog.setMessage(message);
				}
			}
		});
	}
	
	public static void setTitleAndMessage(final Activity activity, final String title, final String message) {
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(syncProgressDialog != null) {
					syncProgressDialog.setTitle(title);
					syncProgressDialog.setMessage(message);
				}
			}
		});
	}
	
	public static void show(final Activity activity) {
		String title = activity.getResources().getString(R.string.sync_in_progress);
		String message = activity.getResources().getString(R.string.sync_in_progress_msg);
		show(activity, title, message);
	}
	
	public static void show(final Activity activity, final String title, final String message) {
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				showFromUiThread(activity, title, message);
			}
		});
	}
	
	public static void showFromUiThread(final Activity activity, final String title, final String message) {
		if(syncProgressDialog == null) {
			syncProgressDialog = ProgressDialog.show(activity, title, message, true);
		}
	}
	
	public static void dismiss(final Activity activity) {
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(syncProgressDialog != null) {
					syncProgressDialog.dismiss();
					syncProgressDialog = null;
				}
			}
		});
	}
	
	public static void reset(final Activity activity) {
		String title = activity.getResources().getString(R.string.sync_in_progress);
		String message = activity.getResources().getString(R.string.sync_in_progress_msg);
		
		setTitleAndMessage(activity, title, message);
	}

}
