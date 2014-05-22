package com.lmn.Arbiter_Android.GeometryEditor;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;

import com.lmn.Arbiter_Android.Activities.HasThreadPool;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ControlPanelHelper;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;

public class InsertHandler {
	
	private WeakReference<Activity> weakActivity;
	private Map.CordovaMap cordovaMap;
	private ExecutorService threadPool;
	
	public InsertHandler(Activity activity){
		this.weakActivity = new WeakReference<Activity>(activity);
		this.cordovaMap = null;
		
		if(activity != null){
			try{
				this.cordovaMap = (Map.CordovaMap) activity;
				this.threadPool = ((HasThreadPool) activity).getThreadPool();
			}catch(ClassCastException e){
				e.printStackTrace();
			}
		}
	}
	
	private void clearControlPanelFromPreferences(final Runnable onCompleted){
		final Activity activity = weakActivity.get();
		
		if(activity != null){
			
			String title = activity.getResources().getString(R.string.loading);
			String message = activity.getResources().getString(R.string.please_wait);
			
			final ProgressDialog dialog = ProgressDialog.show(activity, title, message);
			
			threadPool.execute(new Runnable(){
				@Override
				public void run(){
					ControlPanelHelper helper = new ControlPanelHelper(activity);
					
					helper.clearControlPanel();
					
					activity.runOnUiThread(new Runnable(){
						@Override
						public void run(){
							
							onCompleted.run();
							
							dialog.dismiss();
						}
					});
				}
			});
		}
	}
	
	public void cancel(final Runnable onCancel){
		clearControlPanelFromPreferences(new Runnable(){
			@Override
			public void run(){
				Map.getMap().resetWebApp(cordovaMap.getWebView());
				
				onCancel.run();
			}
		});
	}
	
	public void done(){
		Log.w("InsertHandler", "InsertHandler done");
		Map.getMap().getUpdatedGeometry(cordovaMap.getWebView());
	}
}
