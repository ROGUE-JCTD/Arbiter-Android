package com.lmn.Arbiter_Android;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;

import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class ArbiterProject {
	private static final String ARBITER_PREFERENCES = "ArbiterPreferences";
	
	private static final String OPEN_PROJECT_ID = "openProjectId";
	private static final String OPEN_PROJECT_NAME = "openProjectName";
	
	// The id of the project that was open.
	// This is used in MapActivity to decide whether or not
	// to load the map
	private long oldProjectId = -1;
	private long openProjectId = -1;
	
	private String openProjectName = null;
	
	
	private boolean includeDefaultLayer = true;
	private boolean defaultLayerVisibility = true;
	
	private boolean isSettingAOI = false;
	
	private ArbiterProject(){}
	
	private static ArbiterProject project = null;
	private Project newProject;
	
	private String savedBounds = null;
	private String savedZoomLevel = null;
	
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
	public void setOpenProject(Context context, long projectId, 
			String projectName, boolean includeDefaultLayer){
		
		// Save the open project id to shared preferences for persistent storage
		SharedPreferences settings = context.getSharedPreferences(ARBITER_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(OPEN_PROJECT_ID, projectId);
		editor.putString(OPEN_PROJECT_NAME, projectName);
		
		editor.commit();
		
		// Set the new project Id
		this.openProjectId = projectId;
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
	public long getOpenProject(Activity activity){
		if(!openProjectHasBeenInitialized()){
			Context context = activity.getApplicationContext();
			
			// projectId hasn't been set yet so get the
			// last open project from SharedPreferences
    		SharedPreferences settings = context.
    				getSharedPreferences(ARBITER_PREFERENCES, FragmentActivity.MODE_PRIVATE);
    		
    		openProjectId = settings.getLong(OPEN_PROJECT_ID, -1);
    		openProjectName = settings.getString(OPEN_PROJECT_NAME, null);
    		
    		ApplicationDatabaseHelper helper = ApplicationDatabaseHelper
	    			.getHelper(context);
    		
    		// If openProject is STILL -1, then there wasn't a previously opened project  
    		if(!openProjectHasBeenInitialized()){
    			openProjectId = ProjectStructure.
    					getProjectStructure().ensureProjectExists(activity);
    			openProjectName = context.getResources().getString(R.string.default_project_name);
    		}
    		
    		// Get whether or not this project includes the default layer
    		// and if so, is it visible
    		boolean[] defaultLayerInfo = ProjectsHelper.getProjectsHelper().
					getIncludeDefaultLayer(helper.getWritableDatabase(), context, openProjectId);
    		
    		if(defaultLayerInfo != null){
        		setIncludeDefaultLayer(defaultLayerInfo[0]);
        		setDefaultLayerVisibility(defaultLayerInfo[1]);
    		}
    	}
		
		return openProjectId;
	}
	
	public String getOpenProjectName(Activity activity){
		getOpenProject(activity);
		
		return this.openProjectName;
	}
	
	private boolean openProjectHasBeenInitialized(){
		return openProjectId != -1;
	}
	
	public boolean isSameProject(){
		return oldProjectId == openProjectId;
	}
	
	public void makeSameProject(){	
		oldProjectId = openProjectId;
	}
	
	public void createNewProject(String name){
		newProject = new Project(-1, name, "", true, true);
	}
	
	public Project getNewProject(){
		return newProject;
	}
	
	public boolean includeDefaultLayer(){
		return this.includeDefaultLayer;
	}
	
	public boolean isSettingAOI(){
		return this.isSettingAOI;
	}
	
	public void isSettingAOI(boolean settingAOI){
		this.isSettingAOI = settingAOI;
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
	
	public boolean getDefaultLayerVisibility(){
		return this.defaultLayerVisibility;
	}
	
	public void setProjectsAOI(final Context context, final long projectId, final String aoi){
		ApplicationDatabaseHelper helper = ApplicationDatabaseHelper.getHelper(context);
		ProjectsHelper.getProjectsHelper().setProjectsAOI(helper.getWritableDatabase(), 
				context, projectId, aoi, new Runnable(){
			
			@Override
			public void run(){
				isSettingAOI(false);
			}
		});
	}
	
	public void setDefaultLayerVisibility(boolean defaultLayerVisibility){
		this.defaultLayerVisibility = defaultLayerVisibility;
	}
	
	public void setIncludeDefaultLayer(boolean includeDefaultLayer){
		this.includeDefaultLayer = includeDefaultLayer;
	}
	
	public void setIncludeDefaultLayer(final Context context, final long projectId, 
			final boolean includeDefaultLayer, final Runnable callback){
		
		ApplicationDatabaseHelper helper = ApplicationDatabaseHelper.getHelper(context);
		ProjectsHelper.getProjectsHelper().setIncludeDefaultLayer(helper.getWritableDatabase(), 
				context, projectId, includeDefaultLayer, new Runnable(){
			@Override
			public void run(){
				setIncludeDefaultLayer(includeDefaultLayer);
				
				callback.run();
			}
		});
	}
	
	public void updateAttributeValues(final Context context, final long projectId, 
            final ContentValues values, final Runnable callback){
    
		ApplicationDatabaseHelper helper = ApplicationDatabaseHelper.getHelper(context);
		ProjectsHelper.getProjectsHelper().updateProjectAttributes(helper.
                    getWritableDatabase(), context, projectId, values, callback);
	}
}
