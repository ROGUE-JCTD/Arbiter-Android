package com.lmn.Arbiter_Android.Loaders;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.BroadcastReceivers.AddTilesetsBroadcastReceiver;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesetsHelper;
import com.lmn.Arbiter_Android.Dialog.Dialogs.AddTilesetDialog;
import com.lmn.Arbiter_Android.Map.Helpers.GetCapabilities;
import com.lmn.Arbiter_Android.R;

import java.io.IOException;
import org.json.JSONException;
import java.util.ArrayList;

public class AddTilesetsListLoader extends AsyncTaskLoader<ArrayList<Tileset>> {
	public static final String ADD_TILESETS_LIST_UPDATED = "ADD_TILESETS_LIST_UPDATED";

	private AddTilesetsBroadcastReceiver loaderBroadcastReceiver = null;
	private AddTilesetDialog dialog = null;
	private GetCapabilities getCapabilities;
	private ArrayList<Tileset> tilesets;
	private ProgressDialog progressDialog;
	private boolean connectedOK;
	private AlertDialog alertDialog;

	public AddTilesetsListLoader(AddTilesetDialog dialog) {
		super(dialog.getActivity().getApplicationContext());
		this.dialog = dialog;
		
		this.getCapabilities = new GetCapabilities();
	}

	@Override
	public ArrayList<Tileset> loadInBackground() {

		final Activity activity = dialog.getActivity();

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progressDialog = ProgressDialog.show(activity, activity.getResources().getString(R.string.loading_tilesets_getcapabilities),
						activity.getResources().getString(R.string.please_wait), true);
			}
		});

		ArrayList<Tileset> _tilesets = null;
		Server server = dialog.getSelectedServer();

		try {

			if(server != null && server.getUrl() != null) {

				_tilesets = getCapabilities.getTilesets(server,
						activity);
			}

			connectedOK = true;
		} catch (NullPointerException e) {
			connectedOK = false;
		}finally{
			
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					
					if(progressDialog != null){
						progressDialog.dismiss();
						
						progressDialog = null;
					}
					
					if(!connectedOK && (alertDialog == null)){
						
						AlertDialog.Builder builder = new AlertDialog.Builder(activity);
						
						builder.setTitle(R.string.could_not_connect);
						
						builder.setMessage(R.string.check_server_and_network);
						
						builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
								alertDialog = null;
							}
						});
						
						alertDialog = builder.create();
						
						alertDialog.show();
					}
				}
			});
		}
		
		return _tilesets;
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
        
        ArrayList<Tileset> oldTilesets = _tilesets;
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

        	loaderBroadcastReceiver = new AddTilesetsBroadcastReceiver(this);

        	LocalBroadcastManager.getInstance(getContext()).
        		registerReceiver(loaderBroadcastReceiver, new IntentFilter(AddTilesetsListLoader.ADD_TILESETS_LIST_UPDATED));
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
    protected void onReleaseResources(ArrayList<Tileset> _tilesets) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    	
    }
}
