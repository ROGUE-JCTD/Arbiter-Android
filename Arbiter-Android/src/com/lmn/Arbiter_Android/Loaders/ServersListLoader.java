package com.lmn.Arbiter_Android.Loaders;

import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

import com.lmn.Arbiter_Android.BroadcastReceivers.ServerBroadcastReceiver;
import com.lmn.Arbiter_Android.DatabaseHelpers.DbHelpers;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.ListItems.ServerListItem;

public class ServersListLoader extends AsyncTaskLoader<ServerListItem[]> {
	public static final String SERVER_LIST_UPDATED = "SERVER_LIST_UPDATED";
	
	private ServerBroadcastReceiver loaderBroadcastReceiver = null;
	private ServerListItem[] servers;
	private GlobalDatabaseHelper globalDbHelper = null;
	
	public ServersListLoader(Context context) {
		super(context);
		
		globalDbHelper = DbHelpers.getDbHelpers(context).getGlobalDbHelper();
	}

	@Override
	public ServerListItem[] loadInBackground() {
		ServerListItem[] servers = globalDbHelper.getServers();
		
		return servers;
	}
	
	/**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(ServerListItem[] _servers) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (servers != null) {
          //      onReleaseResources(cursor);
            }
        }
        
        ServerListItem[] oldServers = _servers;
        servers = _servers;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(servers);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldServers != null) {
            onReleaseResources(oldServers);
        }
    }
    
    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (servers != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(servers);
        }

        // Start watching for changes in the app data.
        if (loaderBroadcastReceiver == null) {
        	loaderBroadcastReceiver = new ServerBroadcastReceiver(this);
        	LocalBroadcastManager.getInstance(getContext()).
        		registerReceiver(loaderBroadcastReceiver, 
        				new IntentFilter(ServersListLoader.SERVER_LIST_UPDATED));
        }

        if (takeContentChanged() || servers == null) {
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
    @Override public void onCanceled(ServerListItem[] _servers) {
        super.onCanceled(_servers);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(_servers);
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
        if (servers != null) {
            onReleaseResources(servers);
            servers = null;
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
    protected void onReleaseResources(ServerListItem[] _servers) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    	
    }
}
