package com.lmn.Arbiter_Android.LoaderCallbacks;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.lmn.Arbiter_Android.ListAdapters.ServerListAdapter;
import com.lmn.Arbiter_Android.ListItems.ServerListItem;
import com.lmn.Arbiter_Android.Loaders.ServersListLoader;

public class ServerLoaderCallbacks implements LoaderManager.LoaderCallbacks<ServerListItem[]>{

	private ServerListAdapter serverAdapter;
	private DialogFragment fragment;
	
	public ServerLoaderCallbacks(DialogFragment fragment, ServerListAdapter adapter, int id){
		this.serverAdapter = adapter;
		this.fragment = fragment;
		
		fragment.getActivity().getSupportLoaderManager().initLoader(id, null, this);
	}
	
	@Override
	public Loader<ServerListItem[]> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new ServersListLoader(this.fragment.getActivity().getApplicationContext());
	}

	@Override
	public void onLoadFinished(Loader<ServerListItem[]> loader, ServerListItem[] data) {
		serverAdapter.setData(data);
	}

	@Override
	public void onLoaderReset(Loader<ServerListItem[]> loader) {
		serverAdapter.setData(null);
	}	
}
