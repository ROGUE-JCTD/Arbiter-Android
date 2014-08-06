package com.lmn.Arbiter_Android;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lmn.Arbiter_Android.BaseClasses.BaseLayer;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class InsertProjectHelper {
	private ArbiterProject arbiterProject;
	private Activity activity;
	private Map.CordovaMap cordovaMap;
	private Project newProject;
	
	public InsertProjectHelper(Activity activity, Project newProject){
		this.arbiterProject = ArbiterProject.getArbiterProject();
		this.newProject = newProject;
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
				final long[] layerIds = insertNewProject();
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						arbiterProject.makeSameProject();
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
				newProject.getLayers(), layerIds);
	}
	
	private long[] insertNewProject(){
		Context context = activity.getApplicationContext();
		
		String projectName = newProject.getProjectName();
		
		Log.w("InsertProjectHelper", "InsertProjectHelper.insertNewProject projectName = " + projectName);
		
		// Create the project directory
		createProjectDirectory(activity, projectName);
		
		// Set the open project to the new project
		setOpenProject(context, newProject);
		
		// Open the db connection to the new project, which
		// will create the new databases for the new project.
		ProjectDatabaseHelper helper = getProjectDatabaseHelper(context, projectName);
		
		insertProjectSettings(helper.getWritableDatabase(), context, newProject);
		
		// Insert the layers into the new project
		long[] layerIds = insertLayers(helper, context, newProject);
		
		// Insert the base layer into the new project
		try {
			insertBaseLayer(helper.getWritableDatabase(), context, newProject.getBaseLayer());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Insert the aoi and default layer info
		insertProjectInfo(helper, context, newProject);
		
		return layerIds;
	}
	
	private void insertProjectSettings(SQLiteDatabase projectDb, Context context, Project newProject){
		
		PreferencesHelper.getHelper().put(projectDb, context, PreferencesHelper.DOWNLOAD_PHOTOS, newProject.shouldDownloadPhotos());
		PreferencesHelper.getHelper().put(projectDb, context, PreferencesHelper.DISABLE_WMS, newProject.shouldDisableWMS());
		PreferencesHelper.getHelper().put(projectDb, context, PreferencesHelper.NO_CON_CHECKS, newProject.shouldCheckConnections());
		PreferencesHelper.getHelper().put(projectDb, context, PreferencesHelper.ALWAYS_SHOW_LOCATION, newProject.shouldAlwaysShowLocation());
	}
	
	private void insertBaseLayer(SQLiteDatabase db, Context context, BaseLayer baseLayer) throws JSONException{
		
		if(baseLayer != null){
			PreferencesHelper.getHelper().put(db, context, PreferencesHelper.BASE_LAYER, "[" + baseLayer.getJSON().toString() + "]");
		}
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
		
		Log.w("InsertProjectHelper", "InsertProjectHelper: aoi = " + newProject.getAOI());
		PreferencesHelper.getHelper().put(helper.getWritableDatabase(), 
				context, ArbiterProject.AOI, newProject.getAOI());
		
		PreferencesHelper.getHelper().put(helper.getWritableDatabase(),
				context, ArbiterProject.PROJECT_NAME, newProject.getProjectName());
	}
}
