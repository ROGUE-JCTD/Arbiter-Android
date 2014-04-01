package com.lmn.Arbiter_Android.Loaders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.BaseClasses.BaseLayer;
import com.lmn.Arbiter_Android.BroadcastReceivers.BaseLayerBroadcastReceiver;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class BaseLayerLoader extends AsyncTaskLoader<JSONArray> {
	public static final String BASE_LAYER_CHANGED = "BASE_LAYER_UPDATED";
	
	private BaseLayerBroadcastReceiver loaderBroadcastReceiver = null;
	private JSONArray baseLayers;
	private Context context;
	private Activity activity;
	
	public BaseLayerLoader(Activity activity) {
		super(activity.getApplicationContext());
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}

	@Override
	public JSONArray loadInBackground() {
		
		String baseLayersStr =  PreferencesHelper.getHelper().get(getDb(), context, PreferencesHelper.BASE_LAYER);
		
		try {
			
			if(baseLayersStr != null && !baseLayersStr.equals("") && !baseLayersStr.equals("null")){
				baseLayers = new JSONArray(baseLayersStr);
			}else{
				baseLayers = new JSONArray();
			}
		
			if(baseLayers.length() == 0){
				baseLayers.put(new JSONObject(
						"{'" + BaseLayer.NAME + "': 'OpenStreetMap', '" 
							+ BaseLayer.URL + "': null, '"
							+ BaseLayer.SERVER_ID + "': 'OpenStreetMap', '"
							+ BaseLayer.SERVER_NAME + "': 'OpenStreetMap', '"
							+ BaseLayer.FEATURE_TYPE + "': null}"));
			}
			
			Log.w("BaseLayerLoader", "BaseLayerLoader.loadInBackground baseLayers: " + baseLayers.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return baseLayers;
	}
	
	private SQLiteDatabase getDb(){
		String projectName = ArbiterProject.getArbiterProject().getOpenProject(activity);
		
		return ProjectDatabaseHelper.getHelper(context, ProjectStructure.getProjectPath(projectName),false).getWritableDatabase();
	}
	
	/**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(JSONArray _baseLayers) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (baseLayers != null) {
          //      onReleaseResources(cursor);
            }
        }
        
        JSONArray oldBaseLayers = baseLayers;
        baseLayers = _baseLayers;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(baseLayers);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldBaseLayers != null) {
            onReleaseResources(oldBaseLayers);
        }
    }
    
    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (baseLayers != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(baseLayers);
        }

        // Start watching for changes in the app data.
        if (loaderBroadcastReceiver == null) {
        	loaderBroadcastReceiver = new BaseLayerBroadcastReceiver(this);
        	LocalBroadcastManager.getInstance(getContext()).
        		registerReceiver(loaderBroadcastReceiver, 
        				new IntentFilter(BaseLayerLoader.BASE_LAYER_CHANGED));
        }

        if (takeContentChanged() || baseLayers == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override public void onCanceled(JSONArray _baseLayers) {
        super.onCanceled(_baseLayers);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(_baseLayers);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (baseLayers != null) {
            onReleaseResources(baseLayers);
            baseLayers = null;
        }

        // Stop monitoring for changes.
        if (loaderBroadcastReceiver != null) {
        	LocalBroadcastManager.getInstance(getContext()).
        		unregisterReceiver(loaderBroadcastReceiver);
            loaderBroadcastReceiver = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(JSONArray _baseLayers) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    	
    }
}
