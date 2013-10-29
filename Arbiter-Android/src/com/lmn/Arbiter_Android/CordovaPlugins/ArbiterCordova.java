package com.lmn.Arbiter_Android.CordovaPlugins;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.util.Log;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;

public class ArbiterCordova extends CordovaPlugin{
	private final ArbiterProject arbiterProject;
	
	public ArbiterCordova(){
		super();
		this.arbiterProject = ArbiterProject.getArbiterProject();
	}
	
	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		
		if("setNewProjectsAOI".equals(action)){
			
			String aoi = args.getString(0);
			
			setNewProjectsAOI(aoi, callbackContext);
			
			return true;
		}
		
		// Returning false results in a "MethodNotFound" error.
		return false;
	}
	
	/**
	 * Set the ArbiterProject Singleton's newProject aoi, commit the project, and return to the map
	 */
	public void setNewProjectsAOI(final String aoi, final CallbackContext callbackContext){
		final Activity activity = this.cordova.getActivity();
		Log.w("ARBITERCORDOVA", "ARBITERCORDOVA aoi: " + aoi);
		
		cordova.getActivity().runOnUiThread(new Runnable(){

			@Override
			public void run() {
				ArbiterProject.getArbiterProject().getNewProject().setAOI(aoi);
				
				CommandExecutor.runProcess(new Runnable(){

    				@Override
    				public void run() {
    					GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(activity.getApplicationContext());
    					ProjectsHelper.getProjectsHelper().insert(helper.
    							getWritableDatabase(), activity.getApplicationContext(),
    							arbiterProject.getNewProject());
    					
    					callbackContext.success(); // Thread-safe.
    					
    					activity.finish();
    				}
    				
    			});
			}
			
		});
	}
}
