package com.lmn.Arbiter_Android.Loaders;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;

public class MapLoader extends AsyncTaskLoader<ArrayList<Layer>> {
	public static final String PROJECT_LIST_UPDATED = "PROJECT_LIST_UPDATED";
	
	private ArrayList<Layer> layers;
	private GlobalDatabaseHelper globalDbHelper = null;
	private Context context;
	
	public MapLoader(Context context) {
		super(context);
		this.context = context;
		globalDbHelper = GlobalDatabaseHelper.getGlobalHelper(context);
	}

	@Override
	public ArrayList<Layer> loadInBackground() {
		ArrayList<Layer> layers = LayersHelper.getLayersHelper().
				getAll(globalDbHelper.getWritableDatabase(), 
						ArbiterProject.getArbiterProject().getOpenProject(context));
		
		return layers;
	}
	
	/**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(ArrayList<Layer> _layers) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (layers != null) {
          //      onReleaseResources(cursor);
            }
        }
        
        ArrayList<Layer> oldLayers = _layers;
        layers = _layers;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(layers);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldLayers != null) {
            onReleaseResources(oldLayers);
        }
    }
    
    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (layers != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(layers);
        }

        if (takeContentChanged() || layers == null) {
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
    @Override public void onCanceled(ArrayList<Layer> _layers) {
        super.onCanceled(_layers);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(_layers);
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
        if (layers != null) {
            onReleaseResources(layers);
            layers = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(ArrayList<Layer> _layers) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    	
    }
}
