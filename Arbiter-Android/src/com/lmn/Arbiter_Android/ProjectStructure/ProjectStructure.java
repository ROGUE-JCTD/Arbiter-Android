package com.lmn.Arbiter_Android.ProjectStructure;

import java.io.File;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;

import android.app.Activity;
import android.content.Context;

public class ProjectStructure {
	private ProjectAlerts alerts;
	
	private ProjectStructure(){
		alerts = new ProjectAlerts();
	}
	
	private static ProjectStructure projectStructure = null;
	
	public static ProjectStructure getProjectStructure(){
		if(projectStructure == null){
			projectStructure = new ProjectStructure();
		}
		
		return projectStructure;
	}
	
	public long ensureProjectExists(Activity activity){
		Context context = activity.getApplicationContext();
		String defaultName = context.getResources().
				getString(R.string.default_project_name);
		
		boolean createdNewProject = createProject(activity, defaultName, true);
		
		long projectId = -1;
		
		if(createdNewProject){
			ApplicationDatabaseHelper helper = 
					ApplicationDatabaseHelper.getHelper(context);
			
			projectId = ProjectsHelper.getProjectsHelper().
				ensureProjectExists(helper.getWritableDatabase(), context);
		}
		
		return projectId;
	}
	
	public boolean createProject(Activity activity, String projectName, boolean ensureProjectExists){
		Context context = activity.getApplicationContext();
		String path = getProjectPath(context, projectName);
		
		boolean createdNewProject = false;
		
		// Create the project folder
		if(!projectExists(context, path)){
			createdNewProject = createProjectDirectory(activity, path);
		}else if(!ensureProjectExists){
			alerts.alertProjectAlreadyExists(activity);
		}
		
		// Create the application database inside the folder
		createApplicationDatabase(context, projectName);
		
		// Create the feature database inside the folder
		createFeatureDatabase(context, projectName);
		
		return createdNewProject;
	}
	
	public void deleteProject(Activity activity, String projectName){
		Context context = activity.getApplicationContext();
		String path = getProjectPath(context, projectName);
		
		deleteProjectFolder(activity, path);
	}
	
	private void deleteProjectFolder(Activity activity, String path){
		File directory = new File(path);
		
		deleteDirectory(directory);
	}
	
	/**
	 * Recursively delete the file
	 * @param file
	 */
	private void deleteDirectory(File file){
		if(!file.exists()){
			return;
		}
		
		if(file.isDirectory()){
			for(File temp : file.listFiles()){
				deleteDirectory(temp);
			}
		}
		
		file.delete();
	}
	
	public static String getProjectPath(Context context, String projectName){
		return context.getFilesDir().toString() + File.separator + projectName;
	}
	
	private void createApplicationDatabase(Context context, String projectName){
		ProjectDatabaseHelper.getHelper(context, getProjectPath(context, projectName));
	}
	
	private void createFeatureDatabase(Context context, String projectName){
		//FeatureDatabaseHelper.getHelper(context, projectName);
	}
	
	private boolean createProjectDirectory(Activity activity, String path){
		boolean successfullyCreated = createDirectory(activity.getApplicationContext(), path);
		
		if(!successfullyCreated){
			alerts.alertCreateProjectFailed(activity);
		}
		
		return successfullyCreated;
	}
	
	private boolean createDirectory(Context context, String path){
		return new File(path).mkdir();
	}
	
	private boolean projectExists(Context context, String path){
		File project = new File(path);
		
		return project.exists();
	}
}
