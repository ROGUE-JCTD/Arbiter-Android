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
import android.os.Environment;

public class ProjectStructure {
	private ProjectAlerts alerts;
	public static final String ROOT_PATH = "Arbiter";
	public static final String TILESETS_PATH = "TileSets";
	public static final String PROJECTS_PATH = "Projects";
	public static final String MEDIA_PATH = "Media";
	private static final String DEFAULT_PROJECT_AOI = "";
	
	private ProjectStructure(){
		alerts = new ProjectAlerts();
	}
	
	private static ProjectStructure projectStructure = null;
	
	public static ProjectStructure getProjectStructure(){
		if(projectStructure == null){
			projectStructure = new ProjectStructure();
			new File(getApplicationRoot()).mkdir();
			new File(getProjectsRoot()).mkdir();
		}
		
		return projectStructure;
	}
	
	public boolean ensureProjectExists(final Activity activity){
		Context context = activity.getApplicationContext();
		final String defaultName = context.getResources().
				getString(R.string.default_project_name);
		
		boolean projectExists = projectExists();
		
		// A project already exists so return.
		if(projectExists){
			return false;
		}
		
		// A project doesn't exist yet so create the root projects folder
		createProjectsRoot();
		
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
						getProjectPath(defaultProjectName), false);
		
		PreferencesHelper.getHelper().put(helper.getWritableDatabase(), 
				context, ArbiterProject.AOI, DEFAULT_PROJECT_AOI);
		
		PreferencesHelper.getHelper().put(helper.getWritableDatabase(),
				context, ArbiterProject.PROJECT_NAME, defaultProjectName);
	}
	
	private boolean projectExists(){
		Project[] projects = getProjects();
		
		return (projects.length > 0); 
	}
	
	public boolean createProject(Activity activity, String projectName, boolean ensureProjectExists){
		Context context = activity.getApplicationContext();
		String path = getProjectPath(projectName);
		
		boolean createdNewProject = false;
		
		// Create the project folder
		if(!projectAlreadyExists(path)){
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
		
		String path = getProjectPath(projectName);
		
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
			projectName = getNextOpenProject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ArbiterProject.getArbiterProject().setOpenProject(
				activity.getApplicationContext(), projectName);
	}
	
	/**
	 * Get the next project to open
	 * @return 
	 * @throws Exception
	 */
	private String getNextOpenProject() throws Exception{
		Project[] projects = getProjects();
		
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
	
	private void createProjectsRoot(){
		createDirectory(getProjectsRoot());
	}
	
	public static String getApplicationRoot(){
		return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + ROOT_PATH ;
	}
	
	public static String getProjectsRoot(){
		return getApplicationRoot() + File.separator + PROJECTS_PATH;
	}
	
	public static String getTileSetsRoot(){
		return getApplicationRoot() + File.separator + TILESETS_PATH + File.separator;
	}
	
	public static String getProjectPath(String projectName){
		return getProjectsRoot() + File.separator + projectName;
	}
	
	public static String getMediaPath(String projectName){
		return getProjectPath(projectName) + File.separator + MEDIA_PATH;
	}
	
	private void createProjectDatabase(Context context, String projectName, boolean ensureProjectExists){
		ProjectDatabaseHelper.getHelper(context, 
				getProjectPath(projectName), ensureProjectExists);
	}
	
	private void createFeatureDatabase(Context context, String projectName, boolean ensureProjectExists){
		FeatureDatabaseHelper.getHelper(context, 
				getProjectPath(projectName), ensureProjectExists);
	}
	
	private boolean createProjectDirectory(Activity activity, String path){
		boolean successfullyCreated = createDirectory(path);
		
		if(!successfullyCreated){
			alerts.alertCreateProjectFailed(activity);
		}
		
		return successfullyCreated;
	}
	
	private boolean createDirectory(String path){
		return new File(path).mkdir();
	}
	
	private boolean projectAlreadyExists(String path){
		File project = new File(path);
		
		return project.exists();
	}
	
	public Project[] getProjects(){
		File rootDir = new File(getProjectsRoot());
		String[] list = rootDir.list();
		
		if(list == null){
			return new Project[0];
		}
		
		Project[] projects = new Project[list.length];
		
		for(int i = 0; i < list.length; i++){
			projects[i] = new Project(list[i], null);
		}
		
		return projects;
	}
}
