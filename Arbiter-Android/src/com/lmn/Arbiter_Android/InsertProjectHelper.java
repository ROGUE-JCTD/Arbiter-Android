package com.lmn.Arbiter_Android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class InsertProjectHelper {
	private ArbiterProject arbiterProject;
	private Activity activity;
	private Map.CordovaMap cordovaMap;
	
	public InsertProjectHelper(Activity activity){
		this.arbiterProject = ArbiterProject.getArbiterProject();
		this.activity = activity;
		
		try {
			cordovaMap = (Map.CordovaMap) activity;
		} catch (ClassCastException e){
			throw new ClassCastException(activity.toString() 
					+ " must implement Map.CordovaMap");
		}
	}
	
	public void insert(){
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				final long[] layerIds = insertNewProject(activity);
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						//Map.getMap().resetWebApp(cordovaMap.getWebView());
						performFeatureDbWork(layerIds);
					}
				});
			}
		});
	}
	
	/**
	 * Make the describeFeatureType request, add the featureType
	 * to the GeometryColumns table, create the featureType's 
	 * table, download and insert the features in the
	 * projects aoi into the featureType's table
	 */
	private void performFeatureDbWork(long[] layerIds){
		Map.getMap().createProject(cordovaMap.getWebView(), 
				arbiterProject.getNewProject().getLayers(), layerIds);
	}
	
	private long[] insertNewProject(final Activity activity){
		Context context = activity.getApplicationContext();
		
		Project newProject = arbiterProject.getNewProject();
		
		String projectName = newProject.getProjectName();
		
		Log.w("InsertProjectHelper", "InsertProjectHelper.insertNewProject projectName = " + projectName);
		
		// Create the project directory
		createProjectDirectory(activity, projectName);
		
		// Set the open project to the new project
		setOpenProject(context, newProject);
		
		// Open the db connection to the new project, which
		// will create the new databases for the new project.
		ProjectDatabaseHelper helper = getProjectDatabaseHelper(context, projectName);
		
		// Insert the layers into the new project
		long[] layerIds = insertLayers(helper, context, newProject);
		
		// Insert the aoi and default layer info
		insertProjectInfo(helper, context, newProject);
		
		return layerIds;
	}
	
	private ProjectDatabaseHelper getProjectDatabaseHelper(Context context, String projectName){
		return ProjectDatabaseHelper.getHelper(context, 
				ProjectStructure.getProjectPath(projectName), false);
	}
	
	private void setOpenProject(Context context, Project newProject){
		// Set the open project
		ArbiterProject.getArbiterProject().setOpenProject(context, 
			newProject.getProjectName());
		
		Log.w("InsertProjectHelper", "InsertProjectHelper.setOpenProject projectName = " + newProject.getProjectName());
	}
	
	private void createProjectDirectory(Activity activity, String projectName){
		// Create the project directory
		ProjectStructure.getProjectStructure().
			createProject(activity, projectName, false);
	}
	
	private long[] insertLayers(ProjectDatabaseHelper helper, Context context, Project newProject){
		// Save all of the layers to the project database
		 return LayersHelper.getLayersHelper().insert(helper.
			getWritableDatabase(), context, newProject.getLayers());
	}
	
	private void insertProjectInfo(ProjectDatabaseHelper helper, Context context, Project newProject){
		String includeDefaultLayer = newProject.includeDefaultLayer();
		
		Log.w("InsertProjectHelper", "InsertProjectHelper: aoi = " + newProject.getAOI());
		PreferencesHelper.getHelper().insert(helper.getWritableDatabase(), 
				context, ArbiterProject.AOI, newProject.getAOI());
		
		Log.w("InsertProjectHelper", "InsertProjectHelper: includeDefaultLayer = " + includeDefaultLayer);
		PreferencesHelper.getHelper().insert(helper.getWritableDatabase(), 
				context, ArbiterProject.INCLUDE_DEFAULT_LAYER, includeDefaultLayer);
		
		PreferencesHelper.getHelper().insert(helper.getWritableDatabase(), 
				context, ArbiterProject.DEFAULT_LAYER_VISIBILITY, includeDefaultLayer);
		
		PreferencesHelper.getHelper().insert(helper.getWritableDatabase(),
				context, ArbiterProject.PROJECT_NAME, newProject.getProjectName());
		
		// ArbiterProject's setOpenProject usually loads this info
		// from the project's projectDb, but since this was ran
		// before we set this info, we need to set it manually.
		arbiterProject.setIncludeDefaultLayer(includeDefaultLayer);
		arbiterProject.setDefaultLayerVisibility(includeDefaultLayer);
	}
}
