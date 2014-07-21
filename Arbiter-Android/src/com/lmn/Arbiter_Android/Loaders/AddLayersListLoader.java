package com.lmn.Arbiter_Android.Loaders;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.BroadcastReceivers.AddLayersBroadcastReceiver;
import com.lmn.Arbiter_Android.Dialog.Dialogs.AddLayersDialog;
import com.lmn.Arbiter_Android.Map.Helpers.GetCapabilities;

public class AddLayersListLoader extends AsyncTaskLoader<ArrayList<Layer>> {
	public static final String ADD_LAYERS_LIST_UPDATED = "ADD_LAYERS_LIST_UPDATED";
	
	private AddLayersBroadcastReceiver loaderBroadcastReceiver = null;
	private AddLayersDialog dialog = null;
	private GetCapabilities getCapabilities;
	private ArrayList<Layer> layers;
	private ProgressDialog progressDialog;
	private boolean connectedOK;
	private AlertDialog alertDialog;
	
	public AddLayersListLoader(AddLayersDialog dialog) {
		super(dialog.getActivity().getApplicationContext());
		this.dialog = dialog;
		
		this.getCapabilities = new GetCapabilities();
	}

	@Override
	public ArrayList<Layer> loadInBackground() {
		
		final Activity activity = dialog.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				progressDialog = ProgressDialog.show(activity, activity.getResources().getString(R.string.loading_getcapabilities),
						activity.getResources().getString(R.string.please_wait), true);
			}
		});
		
		ArrayList<Layer> _layers = null;
		Server server = dialog.getSelectedServer();
			
		try {
			
			if(server != null && server.getUrl() != null){
				
				_layers = getCapabilities.getLayers(server,
						dialog.getLayersInProject());
			}
			
			connectedOK = true;
		} catch (IOException e) {
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
		
		return _layers;
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

     // Start watching for changes in the app data.
        if (loaderBroadcastReceiver == null) {
        	loaderBroadcastReceiver = new AddLayersBroadcastReceiver(this);
        	LocalBroadcastManager.getInstance(getContext()).
        		registerReceiver(loaderBroadcastReceiver, new IntentFilter(AddLayersListLoader.ADD_LAYERS_LIST_UPDATED));
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
    protected void onReleaseResources(ArrayList<Layer> _layers) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    	
    }
}
