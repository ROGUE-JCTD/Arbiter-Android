package com.lmn.Arbiter_Android;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;

public class ArbiterProject {
	private static final String ARBITER_PREFERENCES = "ArbiterPreferences";
	private static final String OPEN_PROJECT = "openProject";
	
	// The id of the project that was open.
	// This is used in MapActivity to decide whether or not
	// to load the map
	private long oldProjectId = -1;
	
	// The id of the project that is open
	private long projectId = -1;
	
	private boolean includeDefaultLayer = true;
	
	private ArbiterProject(){}
	
	private static ArbiterProject project = null;
	private Project newProject;
	
	public static ArbiterProject getArbiterProject(){
		if(project == null){
			project = new ArbiterProject();
		}
		
		return project;
	}
	
	/**
	 * Update SharedPreferences with the projectId, and keep the open project id 
	 * and whether or not the current project includes the default layer.
	 * 
	 * @param context
	 * @param projectId
	 * @param includeDefaultLayer
	 */
	public void setOpenProject(Context context, long projectId, boolean includeDefaultLayer){
		
		// Save the open project id to shared preferences for persistent storage
		SharedPreferences settings = context.getSharedPreferences(ARBITER_PREFERENCES, FragmentActivity.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(OPEN_PROJECT, projectId);
		editor.commit();
		
		// Set the new project Id
		this.projectId = projectId;
		
		// Set includeDefaultLayer
		setIncludeDefaultLayer(includeDefaultLayer);
	}
	
	/**
	 * Get the open project's id.  The projectId should never be negative, so if it is
	 * that means the projectId either hasn't been read from SharedPreferences yet, or
	 * there are no projects, so create one.
	 * 
	 * @param context
	 * @return
	 */
	public long getOpenProject(Context context){
		if(projectId == -1){
    		SharedPreferences settings = context.
    				getSharedPreferences(ARBITER_PREFERENCES, FragmentActivity.MODE_PRIVATE);
    		
    		projectId = settings.getLong(OPEN_PROJECT, -1);
    		
    		GlobalDatabaseHelper helper = GlobalDatabaseHelper
	    			.getGlobalHelper(context);
    		
    		// If openProject is STILL -1, then there wasn't a previously opened project  
    		if(projectId == -1){
    			projectId = ProjectsHelper.getProjectsHelper().ensureProjectExists(
    	    			helper.getWritableDatabase(), context);
    		}
    		
    		// Get whether or not this project includes the default layer
    		setIncludeDefaultLayer(ProjectsHelper.getProjectsHelper().
					getIncludeDefaultLayer(helper.getWritableDatabase(), context, projectId));
    	}
		
		return projectId;
	}
	
	public boolean isSameProject(){
		return oldProjectId == projectId;
	}
	
	public void makeSameProject(){	
		oldProjectId = projectId;
	}
	
	public void createNewProject(String name){
		newProject = new Project(-1, name, "", true);
	}
	
	public Project getNewProject(){
		return newProject;
	}
	
	public boolean includeDefaultLayer(){
		return this.includeDefaultLayer;
	}
	
	public void setIncludeDefaultLayer(boolean includeDefaultLayer){
		this.includeDefaultLayer = includeDefaultLayer;
	}
}
