package com.lmn.Arbiter_Android;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;

import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;

public class ArbiterProject {
	private static final String ARBITER_PREFERENCES = "ArbiterPreferences";
	private static final String OPEN_PROJECT = "openProject";
	
	private long projectId = -1;
	
	private ArbiterProject(){}
	
	private static ArbiterProject project = null;
	
	public static ArbiterProject getArbiterProject(){
		if(project == null){
			project = new ArbiterProject();
		}
		
		return project;
	}
	
	public void setOpenProject(Context context, long projectId){
		this.projectId = projectId;
		SharedPreferences settings = context.getSharedPreferences(ARBITER_PREFERENCES, FragmentActivity.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(OPEN_PROJECT, projectId);
		editor.commit();
	}
	
	public long getOpenProject(Context context){
		if(projectId == -1){
    		SharedPreferences settings = context.getSharedPreferences(ARBITER_PREFERENCES, FragmentActivity.MODE_PRIVATE);
    		projectId = settings.getLong(OPEN_PROJECT, -1);
    		
    		// If openProject is STILL -1, then there wasn't a previously opened project  
    		if(projectId == -1){
    			GlobalDatabaseHelper helper = GlobalDatabaseHelper
    	    			.getGlobalHelper(context);
    			projectId = ProjectsHelper.getProjectsHelper().ensureProjectExists(
    	    			helper.getWritableDatabase(), context);
    		}
    	}
		
		return projectId;
	}
}
