package com.lmn.Arbiter_Android.CordovaPlugins;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.util.Log;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class ArbiterCordova extends CordovaPlugin{
	private final ArbiterProject arbiterProject;
	public static final String cordovaUrl = "file:///android_asset/www/index.html";
	
	public ArbiterCordova(){
		super();
		this.arbiterProject = ArbiterProject.getArbiterProject();
	}
	
	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		
		if("setNewProjectsAOI".equals(action)){
			
			String aoi = args.getString(0);
			
			setNewProjectsAOI(aoi, callbackContext);
			
			return true;
		}else if("resetWebApp".equals(action)){
			String extent = args.getString(0);
			String zoomLevel = args.getString(1);
			
			resetWebApp(extent, zoomLevel, callbackContext);
			
			return true;
		}
		
		// Returning false results in a "MethodNotFound" error.
		return false;
	}
	
	/**
	 * Set the ArbiterProject Singleton's newProject aoi, commit the project, and return to the map
	 */
	public void setNewProjectsAOI(final String aoi, final CallbackContext callbackContext){
		final Activity activity = this.cordova.getActivity();
		final long projectId = arbiterProject.getOpenProject(activity);
		
		activity.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				
				CommandExecutor.runProcess(new Runnable(){

    				@Override
    				public void run() {
    					
    					if(arbiterProject.isSettingAOI()){
    						// Save the aoi to the open project
    						setTheCurrentAOI(activity, projectId, aoi);
    					}else{
    						insertNewProject(activity, aoi);
    					}
    					
    					callbackContext.success(); // Thread-safe.
    					
    					activity.finish();
    				}
    				
    			});
			}
			
		});
	} 
	
	public void resetWebApp(final String currentExtent, final String zoomLevel, final CallbackContext callbackContext){
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
	
	private void insertNewProject(final Activity activity, String aoi){
		Project newProject = arbiterProject.getNewProject();
		
		ProjectStructure.getProjectStructure().
			createProject(activity, newProject.getProjectName(), false);
		
		newProject.setAOI(aoi);
		
		ApplicationDatabaseHelper helper = 
				ApplicationDatabaseHelper.getHelper(activity.getApplicationContext());
		
		ProjectsHelper.getProjectsHelper().insert(helper.
				getWritableDatabase(), activity.getApplicationContext(),
				newProject);
		
		newProject.isBeingCreated(false);
	}
	
	private void setTheCurrentAOI(final Activity activity, 
			final long projectId, final String aoi){
		Log.w("ArbiterCordova", "ArbiterCordova: aoi = " + aoi);
		arbiterProject.setProjectsAOI(activity.getApplicationContext(), projectId, aoi);
	}
}
