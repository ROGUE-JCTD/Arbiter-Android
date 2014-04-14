package com.lmn.Arbiter_Android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;

import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class ArbiterProject {
	private static final String OPEN_PROJECT_NAME = "openProjectName";
	
	// Keys for preferences table in project db
	public static final String PROJECT_NAME = "project_name";
	public static final String AOI = "aoi";
	
	// The name of the project that was open.
	// This is used in MapActivity to decide whether or not
	// to load the map
	private String oldProjectName = null;
	private String openProjectName = null;
	
	private ArbiterProject(){}
	
	private static ArbiterProject project = null;
	private Project newProject;
	
	private boolean resetDefaultProject;
	
	public static ArbiterProject getArbiterProject(){
		if(project == null){
			project = new ArbiterProject();
		}
		
		return project;
	}
	
	/**
	 * Save the last open project
	 * 
	 * @param context
	 * @param projectId
	 */
	public void setOpenProject(Context context, String projectName){
		
		SQLiteDatabase db = ApplicationDatabaseHelper.getHelper(context).getWritableDatabase();
		
		PreferencesHelper.getHelper().put(db, context, OPEN_PROJECT_NAME, projectName);
		
		// Set the open project
		this.openProjectName = projectName;
	}
	
	/**
	 * Get the open project's id.  The projectId should never be negative, so if it is
	 * that means the projectId either hasn't been read from SharedPreferences yet, or
	 * there are no projects, so create one.
	 * 
	 * @param context
	 * @return
	 */
	public String getOpenProject(Activity activity){
		if(!openProjectHasBeenInitialized()){
			Context context = activity.getApplicationContext();
    		
    		SQLiteDatabase db = ApplicationDatabaseHelper.getHelper(context).getWritableDatabase();
    		
    		openProjectName = PreferencesHelper.getHelper().get(db, context, OPEN_PROJECT_NAME);
    		
    		// If openProject is STILL -1, then there wasn't a previously opened project  
    		if(!openProjectHasBeenInitialized()){
    			ProjectStructure.getProjectStructure().ensureProjectExists(activity);
    			openProjectName = context.getResources().getString(R.string.default_project_name);
    		}
    		
    		oldProjectName = openProjectName;
    	}
		
		return openProjectName;
	}
	
	private boolean openProjectHasBeenInitialized(){
		return openProjectName != null;
	}
	
	public boolean isDefaultProject(Context context){
		String defaultName = context.getResources().
				getString(R.string.default_project_name);
		
		return openProjectName.equals(defaultName);
	}
	
	public boolean isSameProject(Context context){
		boolean isSameProject = (oldProjectName != null ? oldProjectName.equals(openProjectName) : openProjectName == null);
		
		if(isSameProject && 
				// it is the default project and the default
				// project has been reset, so it's not the 
				// same project.
				isDefaultProject(context) && resetDefaultProject){
			
			return false;
		}
		
		return isSameProject;
	}
	
	public void makeSameProject(){	
		oldProjectName = openProjectName;
		resetDefaultProject = false;
	}
	
	public void createNewProject(String name){
		newProject = new Project(name, null);
	}
	
	public void errorCreatingProject(Activity activity){
		doneCreatingProject(activity.getApplicationContext());
		
		displayErrorCreatingProject(activity);
	}
	
	private void displayErrorCreatingProject(final Activity activity){		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(R.string.error_creating_project);
		builder.setIcon(activity.getResources().getDrawable(R.drawable.icon));
		builder.setMessage(R.string.error_creating_project_msg);
		
		builder.create().show();
		
		// Delete the corresponding project directory
		ProjectStructure.getProjectStructure().deleteProject(activity, openProjectName);
		
		Map.CordovaMap cordovaMap;
		
		try {
			cordovaMap = (Map.CordovaMap) activity;
		} catch (ClassCastException e){
			e.printStackTrace();
			throw new ClassCastException(activity.toString() 
					+ " must implement Map.CordovaMap");
		}
		
		Map.getMap().resetWebApp(cordovaMap.getWebView());
		
		makeSameProject();
	}
	
	public void doneCreatingProject(Context context){
		newProject = null;
		
		LocalBroadcastManager.getInstance(context).sendBroadcast(
				new Intent(ProjectsListLoader.PROJECT_LIST_UPDATED));
	}
	
	public Project getNewProject(){
		return newProject;
	}
	
	public void resetDefaultProject(boolean reset){
		this.resetDefaultProject = reset;
	}
}
