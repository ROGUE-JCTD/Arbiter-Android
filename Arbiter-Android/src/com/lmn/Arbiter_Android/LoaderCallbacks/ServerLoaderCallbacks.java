package com.lmn.Arbiter_Android.LoaderCallbacks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;

import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.ListAdapters.ServerListAdapter;
import com.lmn.Arbiter_Android.Loaders.AddLayersListLoader;
import com.lmn.Arbiter_Android.Loaders.ServersListLoader;

public class ServerLoaderCallbacks implements LoaderManager.LoaderCallbacks<SparseArray<Server>>{

	private ServerListAdapter serverAdapter;
	private DialogFragment fragment;
	
	public ServerLoaderCallbacks(DialogFragment fragment, ServerListAdapter adapter, int id){
		this.serverAdapter = adapter;
		this.fragment = fragment;
		
		fragment.getActivity().getSupportLoaderManager().initLoader(id, null, this);
	}
	
	@Override
	public Loader<SparseArray<Server>> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new ServersListLoader(this.fragment.getActivity().getApplicationContext());
	}

	@Override
	public void onLoadFinished(Loader<SparseArray<Server>> loader, SparseArray<Server> data) {
		serverAdapter.setData(data);
		
		// Needed to update the AddLayers list if a new server gets added above
		// the other servers in the dropdown.
		LocalBroadcastManager.getInstance(fragment.getActivity().getApplicationContext()).
			sendBroadcast(new Intent(AddLayersListLoader.ADD_LAYERS_LIST_UPDATED));
	}

	@Override
	public void onLoaderReset(Loader<SparseArray<Server>> loader) {
		serverAdapter.setData(null);
	}	
}
