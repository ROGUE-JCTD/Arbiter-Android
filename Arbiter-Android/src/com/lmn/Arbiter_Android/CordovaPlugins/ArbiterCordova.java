package com.lmn.Arbiter_Android.CordovaPlugins;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.ArbiterState;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class ArbiterCordova extends CordovaPlugin{
	private static final String TAG = "ArbiterCordova";
	private final ArbiterProject arbiterProject;
	public static final String mainUrl = "file:///android_asset/www/main.html";
	public static final String aoiUrl = "file:///android_asset/www/aoi.html";
	//public static final String mainUrl = "content://jsHybugger.org/file:///android_asset/www/main.html";
	//public static final String aoiUrl = "content://jsHybugger.org/file:///android_asset/www/aoi.html";
	
	public ArbiterCordova(){
		super();
		this.arbiterProject = ArbiterProject.getArbiterProject();
	}
	
	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		
		if("setProjectsAOI".equals(action)){
			
			String aoi = args.getString(0);
			
			setProjectsAOI(aoi, callbackContext);
			
			return true;
		}else if("resetWebApp".equals(action)){
			String extent = args.getString(0);
            String zoomLevel = args.getString(1);
            
			resetWebApp(extent, zoomLevel, callbackContext);
			
			return true;
		}else if("setNewProjectsAOI".equals(action)){
			String aoi = args.getString(0);
			
			setNewProjectsAOI(aoi, callbackContext);
			
			return true;
		}else if("doneCreatingProject".equals(action)){
			
			doneCreatingProject();
			
			return true;
		}else if("errorCreatingProject".equals(action)){
			errorCreatingProject();
			
			return true;
		}else if("errorLoadingFeatures".equals(action)){
			errorLoadingFeatures();
			
			return true;
		}
		
		// Returning false results in a "MethodNotFound" error.
		return false;
	}
	
	private void errorLoadingFeatures(){
		final Activity activity = cordova.getActivity();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(R.string.error_loading_features);
		builder.setIcon(activity.getResources().getDrawable(R.drawable.icon));
		builder.setMessage(R.string.error_loading_features_msg);
		
		builder.create().show();
	}
	
	private void errorCreatingProject(){
		ArbiterProject.getArbiterProject().errorCreatingProject(
				cordova.getActivity());
	}
	
	private void doneCreatingProject(){
		ArbiterProject.getArbiterProject().doneCreatingProject(
				cordova.getActivity().getApplicationContext());
	}
	
	private void setNewProjectsAOI(final String aoi, final CallbackContext callbackContext){
		//ArbiterState.getState().setNewAOI(aoi);
		ArbiterProject.getArbiterProject().getNewProject().setAOI(aoi);
		
		callbackContext.success();
		
		cordova.getActivity().finish();
	}
	
	/**
	 * Set the ArbiterProject Singleton's newProject aoi, commit the project, and return to the map
	 */
	private void setProjectsAOI(final String aoi, final CallbackContext callbackContext){
		
		ArbiterState.getState().setNewAOI(aoi);
		
		callbackContext.success();
		
		cordova.getActivity().finish();
	} 
	
	private void resetWebApp(final String currentExtent, final String zoomLevel, final CallbackContext callbackContext){
		final Activity activity = this.cordova.getActivity();
		final CordovaWebView webview = this.webView;
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				arbiterProject.setSavedZoomLevel(zoomLevel);
                arbiterProject.setSavedBounds(currentExtent);
                
				webview.loadUrl("about:blank");
			}
		});
	}
}
