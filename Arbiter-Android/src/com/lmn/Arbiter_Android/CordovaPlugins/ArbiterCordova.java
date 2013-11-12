package com.lmn.Arbiter_Android.CordovaPlugins;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lmn.Arbiter_Android.ArbiterProject;
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
		}else if("createProject".equals(action)){
			String aoi = args.getString(0);
			
			createProject(aoi, callbackContext);
			
			return true;
		}else if("finishActivity".equals(action)){
			finishActivity();
			
			return true;
		}
		
		// Returning false results in a "MethodNotFound" error.
		return false;
	}
	
	public void finishActivity(){
		cordova.getActivity().finish();
	}
	
	public void createProject(final String aoi, final CallbackContext callbackContext){
		final Activity activity = this.cordova.getActivity();
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				insertNewProject(activity, aoi);
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						callbackContext.success();
					}
				});
			}
		});
	}
	
	/**
	 * Set the ArbiterProject Singleton's newProject aoi, commit the project, and return to the map
	 */
	public void setProjectsAOI(final String aoi, final CallbackContext callbackContext){
		final Activity activity = this.cordova.getActivity();
		final String projectName = arbiterProject.getOpenProject(activity);
		
		CommandExecutor.runProcess(new Runnable(){

			@Override
			public void run() {
				// Save the aoi to the open project
				setProjectsAOI(activity.getApplicationContext(), projectName, aoi);
				
				activity.runOnUiThread(new Runnable(){

					@Override
					public void run() {
						callbackContext.success();
						
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
		Context context = activity.getApplicationContext();
		
		Project newProject = arbiterProject.getNewProject();
		
		String projectName = newProject.getProjectName();
		
		newProject.setAOI(aoi);
		
		// Create the project directory
		createProjectDirectory(activity, projectName);
		
		// Set the open project to the new project
		setOpenProject(context, newProject);
		
		// Open the db connection to the new project, which
		// will create the new databases for the new project.
		ProjectDatabaseHelper helper = getProjectDatabaseHelper(context, projectName);
		
		// Insert the layers into the new project
		insertLayers(helper, context, newProject);
		
		// Insert the aoi and default layer info
		insertProjectInfo(helper, context, newProject);
		
		// The project is done being created,
		// so set isBeingCreated to false.
		newProject.isBeingCreated(false);
		
		LocalBroadcastManager.getInstance(context).
        	sendBroadcast(new Intent(ProjectsListLoader.PROJECT_LIST_UPDATED));
	}
	
	private ProjectDatabaseHelper getProjectDatabaseHelper(Context context, String projectName){
		return ProjectDatabaseHelper.getHelper(context, 
				ProjectStructure.getProjectPath(context, 
						projectName));
	}
	
	private void setOpenProject(Context context, Project newProject){
		// Set the open project
		ArbiterProject.getArbiterProject().setOpenProject(context, 
			newProject.getProjectName(), newProject.includeDefaultLayer());
	}
	
	private void createProjectDirectory(Activity activity, String projectName){
		// Create the project directory
		ProjectStructure.getProjectStructure().
			createProject(activity, projectName, false);
	}
	
	private void insertLayers(ProjectDatabaseHelper helper, Context context, Project newProject){
		// Save all of the layers to the project database
		LayersHelper.getLayersHelper().insert(helper.
			getWritableDatabase(), context, newProject.getLayers());
	}
	
	private void insertProjectInfo(ProjectDatabaseHelper helper, Context context, Project newProject){
		PreferencesHelper.getHelper().insert(helper.getWritableDatabase(), 
				context, ArbiterProject.AOI, newProject.getAOI());
		
		PreferencesHelper.getHelper().insert(helper.getWritableDatabase(), 
				context, ArbiterProject.INCLUDE_DEFAULT_LAYER, newProject.includeDefaultLayer());
		
		PreferencesHelper.getHelper().insert(helper.getWritableDatabase(), 
				context, ArbiterProject.DEFAULT_LAYER_VISIBILITY, newProject.includeDefaultLayer());
	}
	
	private void setProjectsAOI(final Context context, final String projectName, final String aoi){
		ProjectDatabaseHelper helper = 
				ProjectDatabaseHelper.getHelper(context, 
						ProjectStructure.getProjectPath(context, projectName));
		
		PreferencesHelper.getHelper().update(
				helper.getWritableDatabase(), context, ArbiterProject.AOI, aoi);
	}
}
