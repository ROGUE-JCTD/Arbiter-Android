package com.lmn.Arbiter_Android.ProjectStructure;

import java.io.File;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;

import android.app.Activity;
import android.content.Context;

public class ProjectStructure {
	private ProjectAlerts alerts;
	private static final String PROJECTS_DIRECTORY_NAME = "projects";
	private static final String DEFAULT_PROJECT_AOI = "-20037508.34,-20037508.34,20037508.34,20037508.34";
	private static final String DEFAULT_INCLUDE_DEFAULT_LAYER = "true";
	private static final String DEFAULT_DEFAULT_LAYER_VISIBILITY = "true";
	
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
	
	public boolean ensureProjectExists(final Activity activity){
		Context context = activity.getApplicationContext();
		final String defaultName = context.getResources().
				getString(R.string.default_project_name);
		
		boolean projectExists = projectExists(context);
		
		// A project already exists so return.
		if(projectExists){
			return false;
		}
		
		// A project doesn't exist yet so create the root projects folder
		createProjectsRoot(context);
		
		// A project doesn't exist yet so create the default project
		boolean projectCreated = createProject(activity, defaultName, true);
		
		if(projectCreated){
			insertDefaultProjectInfo(context, defaultName);
			
			ArbiterProject.getArbiterProject().resetDefaultProject(true);
		}
		
		return projectCreated;
	}
	
	private void insertDefaultProjectInfo(Context context, String defaultProjectName){
		ProjectDatabaseHelper helper = 
				ProjectDatabaseHelper.getHelper(context,
						getProjectPath(context, defaultProjectName));
		
		PreferencesHelper.getHelper().insert(helper.getWritableDatabase(), 
				context, ArbiterProject.AOI, DEFAULT_PROJECT_AOI);
		
		PreferencesHelper.getHelper().insert(helper.getWritableDatabase(), 
				context, ArbiterProject.INCLUDE_DEFAULT_LAYER, DEFAULT_INCLUDE_DEFAULT_LAYER);
		
		PreferencesHelper.getHelper().insert(helper.getWritableDatabase(), 
				context, ArbiterProject.DEFAULT_LAYER_VISIBILITY, DEFAULT_DEFAULT_LAYER_VISIBILITY);
	}
	
	private boolean projectExists(Context context){
		Project[] projects = getProjects(context);
		
		return (projects.length > 0); 
	}
	
	public boolean createProject(Activity activity, String projectName, boolean ensureProjectExists){
		Context context = activity.getApplicationContext();
		String path = getProjectPath(context, projectName);
		
		boolean createdNewProject = false;
		
		// Create the project folder
		if(!projectAlreadyExists(context, path)){
			createdNewProject = createProjectDirectory(activity, path);
		}else if(!ensureProjectExists){
			//alerts.alertProjectAlreadyExists(activity);
		}
		
		// Create the application database inside the folder
		createProjectDatabase(context, projectName, ensureProjectExists);
		
		// Create the feature database inside the folder
		createFeatureDatabase(context, projectName, ensureProjectExists);
		
		return createdNewProject;
	}
	
	public void deleteProject(Activity activity, String projectName){
		String openProjectName = ArbiterProject.getArbiterProject()
				.getOpenProject(activity);
		
		Context context = activity.getApplicationContext();
		String path = getProjectPath(context, projectName);
		
		deleteProjectFolder(activity, path);
		
		// Make sure that a project exists.
		boolean defaultProjectCreated = ensureProjectExists(activity);
		
		if(!defaultProjectCreated && projectName.equals(openProjectName)){
			switchProject(activity);
		}
	}
	
	private void switchProject(Activity activity){
		String projectName = "";
		
		try {
			projectName = getNextOpenProject(activity.getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ArbiterProject.getArbiterProject().setOpenProject(
				activity.getApplicationContext(), projectName);
	}
	
	/**
	 * Get the next project to open
	 * @param context
	 * @return 
	 * @throws Exception
	 */
	private String getNextOpenProject(Context context) throws Exception{
		Project[] projects = getProjects(context);
		
		if(projects.length > 0){
			return projects[0].getProjectName();
		}
		
		throw new Exception("Could not open another project.");
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
	
	private void createProjectsRoot(Context context){
		createDirectory(context, getProjectsRoot(context));
	}
	
	public static String getProjectsRoot(Context context){
		return context.getFilesDir().toString() + File.separator + PROJECTS_DIRECTORY_NAME;
	}
	
	public static String getProjectPath(Context context, String projectName){
		return getProjectsRoot(context) + File.separator + projectName;
	}
	
	private void createProjectDatabase(Context context, String projectName, boolean ensureProjectExists){
		ProjectDatabaseHelper.getHelper(context, 
				getProjectPath(context, projectName), ensureProjectExists);
	}
	
	private void createFeatureDatabase(Context context, String projectName, boolean ensureProjectExists){
		FeatureDatabaseHelper.getHelper(context, 
				getProjectPath(context, projectName), ensureProjectExists);
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
	
	private boolean projectAlreadyExists(Context context, String path){
		File project = new File(path);
		
		return project.exists();
	}
	
	public Project[] getProjects(Context context){
		File rootDir = new File(getProjectsRoot(context));
		String[] list = rootDir.list();
		
		if(list == null){
			return new Project[0];
		}
		
		Project[] projects = new Project[list.length];
		
		for(int i = 0; i < list.length; i++){
			projects[i] = new Project(list[i], null, null, null);
		}
		
		return projects;
	}
}
