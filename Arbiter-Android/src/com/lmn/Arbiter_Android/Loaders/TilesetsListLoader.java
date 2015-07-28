package com.lmn.Arbiter_Android.Loaders;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;

import com.lmn.Arbiter_Android.ArbiterProject;
//import com.lmn.Arbiter_Android.BaseClasses.BaseLayer;
import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.BroadcastReceivers.TilesetBroadcastReceiver;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesetsHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ServersHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class TilesetsListLoader extends AsyncTaskLoader<ArrayList<Tileset>> {
	public static final String TILESETS_LIST_UPDATED = "TILESETS_LIST_UPDATED";

	private TilesetBroadcastReceiver loaderBroadcastReceiver = null;
	private ArrayList<Tileset> tilesets;
	private ProjectDatabaseHelper projectDbHelper = null;
	private ApplicationDatabaseHelper appDbHelper = null;
	private Activity activity;
	private Context context;

	public TilesetsListLoader(Activity activity) {
		super(activity.getApplicationContext());
		this.activity = activity;
		this.context = activity.getApplicationContext();
		
		this.appDbHelper = ApplicationDatabaseHelper.getHelper(this.context);
	}

	public void updateAppDbHelper(){
		this.appDbHelper = ApplicationDatabaseHelper.getHelper(this.context);
	}
	
	@Override
	public ArrayList<Tileset> loadInBackground() {
		updateAppDbHelper();
		
		SQLiteDatabase db = getAppDbHelper().getWritableDatabase();
		
		ArrayList<Tileset> tilesets = TilesetsHelper.getTilesetsHelper().
				getAll(db);
		
		//SparseArray<Server> servers = ServersHelper.getServersHelper().
		//		getAll(getAppDbHelper().getWritableDatabase());
		
		//tilesets = addServerInfoToTilesets(tilesets, servers);
		
		//String json = PreferencesHelper.getHelper().get(db, context, PreferencesHelper.BASE_LAYER);
		
		/*try {
			BaseLayer baseLayer = getBaseLayerFromJSON(json);
			
			layers = removeBaseLayerFromLayers(baseLayer, layers);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		return tilesets;
	}
	
	/*private BaseLayer getBaseLayerFromJSON(String json) throws JSONException{
		
		if(json == null){
			return BaseLayer.createOSMBaseLayer();
		}
		
		JSONArray baseLayers = new JSONArray(json);
		
		return new BaseLayer(baseLayers.getJSONObject(0));
	}
	
	private ArrayList<Layer> removeBaseLayerFromLayers(BaseLayer baseLayer, ArrayList<Layer> layers){
		String featureType = baseLayer.getFeatureType();
		
		if(featureType == null || featureType.equals("null")){
			return layers;
		}
		
		for(int i = 0, count = layers.size(); i < count; i++){
			
			if(featureType.equals(layers.get(i).getFeatureType())){
				layers.remove(i);
				break;
			}
		}
		
		return layers;
	}*/
	
	protected ArrayList<Tileset> addServerInfoToTilesets(ArrayList<Tileset> tilesets,
			SparseArray<Server> servers){
		//Server server;
		
		//for(Tileset tileset : tilesets){
		//	server = servers.get(tileset.getServerId());
		//	tileset.setServerName(server.getName());
		//	tileset.setServerUrl(server.getUrl());
		//}
		
		return tilesets;
	}
	
	protected ProjectDatabaseHelper getProjectDbHelper(){
		return this.projectDbHelper;
	}
	
	protected ApplicationDatabaseHelper getAppDbHelper(){
		return this.appDbHelper;
	}
	
	/**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(ArrayList<Tileset> _tilesets) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (tilesets != null) {
          //      onReleaseResources(cursor);
            }
        }
        
        ArrayList<Tileset> oldTilesets = tilesets;
        tilesets = _tilesets;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(tilesets);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldTilesets != null) {
            onReleaseResources(oldTilesets);
        }
    }
    
    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (tilesets != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(tilesets);
        }

        // Start watching for changes in the app data.
        if (loaderBroadcastReceiver == null) {
        	loaderBroadcastReceiver = new TilesetBroadcastReceiver(this);
        	LocalBroadcastManager.getInstance(getContext()).
        		registerReceiver(loaderBroadcastReceiver, new IntentFilter(TilesetsListLoader.TILESETS_LIST_UPDATED));
        }

        if (takeContentChanged() || tilesets == null) {
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
    @Override public void onCanceled(ArrayList<Tileset> _tilesets) {
        super.onCanceled(_tilesets);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(_tilesets);
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
        if (tilesets != null) {
            onReleaseResources(tilesets);
            tilesets = null;
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
    protected void onReleaseResources(ArrayList<Tileset> _projects) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    	
    }
}
