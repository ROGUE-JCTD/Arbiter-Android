package com.lmn.Arbiter_Android.GeometryEditor;

import java.lang.ref.WeakReference;

import org.apache.cordova.CordovaWebView;

import com.lmn.Arbiter_Android.ArbiterState;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.Map.Map;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class EditHandler {
	private WeakReference<Activity> weakActivity;
	private CordovaWebView cordovaWebView;
	public EditHandler(Activity activity){
		this.weakActivity = new WeakReference<Activity>(activity);
		
		if(activity != null){
			try{
				this.cordovaWebView = ((Map.CordovaMap) activity).getWebView();
			}catch(ClassCastException e){
				e.printStackTrace();
			}
		}
	}
	
	public void cancel(){
		FragmentActivity activity = null;
		
		try{
			activity = (FragmentActivity) weakActivity.get();
		}catch(ClassCastException e){
			e.printStackTrace();
		}
		
		if(activity != null){
			
			// Restore the geometry of the selectedFeature
			Feature selectedFeature = ArbiterState.getArbiterState().isEditingFeature();
			
			// Exit modify mode
			Map.getMap().cancelEdit(cordovaWebView,
					selectedFeature.getOriginalGeometry());
			
			selectedFeature.restoreGeometry();
		}
	}
	
	public void done(){
		Map.getMap().getUpdatedGeometry(cordovaWebView);
	}
}
