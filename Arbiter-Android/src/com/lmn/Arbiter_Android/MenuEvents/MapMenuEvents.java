package com.lmn.Arbiter_Android.MenuEvents;

import android.app.Activity;
import android.content.Intent;

import com.lmn.Arbiter_Android.LayersActivity;
import com.lmn.Arbiter_Android.ModifiedFeaturesActivity;
import com.lmn.Arbiter_Android.ProjectsActivity;
import com.lmn.Arbiter_Android.SettingsActivity;

public class MapMenuEvents extends MenuEvents {

	public MapMenuEvents(){
		super();
	}
	
	/**
	 * Activate add features button
	 */
	public void activateAddFeatures(){
		
	}
	
	/**
	 * Activate remove features button
	 */
	public void activateRemoveFeatures(){
		
	}
	
	/**
	 * Perform sync
	 */
	public void sync(){
		
	}
	
	/**
     * Show the layers page
     */
    public void showLayers(Activity activity){
    	Intent layersIntent = new Intent(activity, LayersActivity.class);
    	activity.startActivity(layersIntent);
    }
    
    /**
     * Show the projects page
     */
    public void showProjects(Activity activity){
    	Intent projectsIntent = new Intent(activity, ProjectsActivity.class);
    	activity.startActivity(projectsIntent);
    }
    
    /**
     * Show the modified features page
     */
    public void showModified(Activity activity){
    	Intent modifiedIntent = new Intent(activity, ModifiedFeaturesActivity.class);
    	activity.startActivity(modifiedIntent);
    }
    
    /**
     * Show the settings page
     */
    public void showSettings(Activity activity){
    	Intent settingsIntent = new Intent(activity, SettingsActivity.class);
    	activity.startActivity(settingsIntent);
    }
}
