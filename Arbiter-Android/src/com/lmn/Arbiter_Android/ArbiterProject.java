package com.lmn.Arbiter_Android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;

import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class ArbiterProject {
	private static final String ARBITER_PREFERENCES = "ArbiterPreferences";
	private static final String OPEN_PROJECT_NAME = "openProjectName";
	
	// Keys for preferences table in project db
	public static final String INCLUDE_DEFAULT_LAYER = "include_default_layer";
	public static final String DEFAULT_LAYER_VISIBILITY = "default_layer_visibility";
	public static final String AOI = "aoi";
	
	// The name of the project that was open.
	// This is used in MapActivity to decide whether or not
	// to load the map
	private String oldProjectName = null;
	private String openProjectName = null;
	
	private String includeDefaultLayer = "true";
	private String defaultLayerVisibility = "true";
	
	private String savedBounds = null;
    private String savedZoomLevel = null;
    
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
	public void setOpenProject(Context context, 
			String projectName, String includeDefaultLayer){
		
		// Save the open project id to shared preferences for persistent storage
		SharedPreferences settings = context.getSharedPreferences(ARBITER_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(OPEN_PROJECT_NAME, projectName);
		
		editor.commit();
		
		// Set the open project
		this.openProjectName = projectName;
		
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
	public String getOpenProject(Activity activity){
		if(!openProjectHasBeenInitialized()){
			Context context = activity.getApplicationContext();
			
			// projectId hasn't been set yet so get the
			// last open project from SharedPreferences
    		SharedPreferences settings = context.
    				getSharedPreferences(ARBITER_PREFERENCES, FragmentActivity.MODE_PRIVATE);
    		
    		openProjectName = settings.getString(OPEN_PROJECT_NAME, null);
    		
    		// If openProject is STILL -1, then there wasn't a previously opened project  
    		if(!openProjectHasBeenInitialized()){
    			ProjectStructure.getProjectStructure().ensureProjectExists(activity);
    			openProjectName = context.getResources().getString(R.string.default_project_name);
    		}
    		
    		// Get whether or not this project includes the default layer
    		// and if so, is it visible
    		String includeDefaultLayer = includeDefaultLayer(context, openProjectName);
    		String defaultLayerVisibility = defaultLayerVisibility(context, openProjectName);
    		
    		if(includeDefaultLayer != null && defaultLayerVisibility != null){
        		setIncludeDefaultLayer(includeDefaultLayer);
        		setDefaultLayerVisibility(defaultLayerVisibility);
    		}
    		
    		oldProjectName = openProjectName;
    	}
		
		return openProjectName;
	}
	
	private boolean openProjectHasBeenInitialized(){
		return openProjectName != null;
	}
	
	public boolean isSameProject(){
		return oldProjectName == openProjectName;
	}
	
	public void makeSameProject(){	
		oldProjectName = openProjectName;
	}
	
	public void createNewProject(String name){
		newProject = new Project(name, "", "true", "true");
	}
	
	public Project getNewProject(){
		return newProject;
	}
	
	public String includeDefaultLayer(){
		return this.includeDefaultLayer;
	}
	
	public String getDefaultLayerVisibility(){
		return this.defaultLayerVisibility;
	}
	
	public void setDefaultLayerVisibility(String defaultLayerVisibility){
		this.defaultLayerVisibility = defaultLayerVisibility;
	}
	
	public void setIncludeDefaultLayer(String includeDefaultLayer){
		this.includeDefaultLayer = includeDefaultLayer;
	}
	
	public void setIncludeDefaultLayer(final Context context, final String projectName, 
			final String includeDefaultLayer){
		ProjectDatabaseHelper helper = ProjectDatabaseHelper.
				getHelper(context, ProjectStructure.getProjectPath(context, projectName));
		
		PreferencesHelper.getHelper().update(
				helper.getWritableDatabase(), context,
				INCLUDE_DEFAULT_LAYER, includeDefaultLayer);
		
		setIncludeDefaultLayer(includeDefaultLayer);
	}
	
	private String includeDefaultLayer(Context context, String projectName){
		ProjectDatabaseHelper helper = ProjectDatabaseHelper.getHelper(context,
				ProjectStructure.getProjectPath(context, projectName));
		
		return PreferencesHelper.getHelper().get(
				helper.getWritableDatabase(), context, INCLUDE_DEFAULT_LAYER);
	}
	
	private String defaultLayerVisibility(Context context, String projectName){
		ProjectDatabaseHelper helper = ProjectDatabaseHelper.getHelper(context,
				ProjectStructure.getProjectPath(context, projectName));
		
		return PreferencesHelper.getHelper().get(
				helper.getWritableDatabase(), context, DEFAULT_LAYER_VISIBILITY);
	}
	
	public String getSavedBounds(){
        return this.savedBounds;
	}
	
	public void setSavedBounds(String savedBounds){
	        this.savedBounds = savedBounds;
	}
	
	public String getSavedZoomLevel(){
	        return this.savedZoomLevel;
	}
	
	public void setSavedZoomLevel(String zoomLevel){
	        this.savedZoomLevel = zoomLevel;
	}
}
