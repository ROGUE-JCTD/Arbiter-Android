package com.lmn.Arbiter_Android.LoaderCallbacks;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.lmn.Arbiter_Android.ListAdapters.LayerListAdapter;
import com.lmn.Arbiter_Android.ListAdapters.ServerListAdapter;
import com.lmn.Arbiter_Android.ListItems.Layer;
import com.lmn.Arbiter_Android.ListItems.ServerListItem;
import com.lmn.Arbiter_Android.Loaders.LayersListLoader;
import com.lmn.Arbiter_Android.Loaders.ServersListLoader;

public class LayerLoaderCallbacks implements LoaderManager.LoaderCallbacks<Layer[]>{

	private LayerListAdapter layerAdapter;
	private DialogFragment fragment;
	
	public LayerLoaderCallbacks(DialogFragment fragment, LayerListAdapter adapter, int id){
		this.layerAdapter = adapter;
		this.fragment = fragment;
		
		fragment.getActivity().getSupportLoaderManager().initLoader(id, null, this);
	}
	
	@Override
	public Loader<Layer[]> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new LayersListLoader(this.fragment.getActivity().getApplicationContext());
	}

	@Override
	public void onLoadFinished(Loader<Layer[]> loader, Layer[] data) {
		layerAdapter.setData(data);
	}

	@Override
	public void onLoaderReset(Loader<Layer[]> loader) {
		layerAdapter.setData(null);
	}	
}
