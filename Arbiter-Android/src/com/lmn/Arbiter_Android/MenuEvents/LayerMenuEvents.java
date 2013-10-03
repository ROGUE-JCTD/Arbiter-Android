package com.lmn.Arbiter_Android.MenuEvents;

import android.app.Activity;
import android.content.Intent;

import com.lmn.Arbiter_Android.AddLayersActivity;

public class LayerMenuEvents extends MenuEvents {

	public LayerMenuEvents(){
		super();
	}
	
	/**
     * Show the add layers page
     */
    public void showAddLayers(Activity activity){
    	Intent layersIntent = new Intent(activity, AddLayersActivity.class);
    	activity.startActivity(layersIntent);
    }
}
