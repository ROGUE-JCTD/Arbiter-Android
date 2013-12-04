package com.lmn.Arbiter_Android.LoaderCallbacks;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.ListAdapters.ArbiterAdapter;
import com.lmn.Arbiter_Android.Loaders.LayersListLoader;

public class LayerLoaderCallbacks implements LoaderManager.LoaderCallbacks<ArrayList<Layer>>{

	private ArbiterAdapter<ArrayList<Layer>> layerAdapter;
	private FragmentActivity activity;
	
	public LayerLoaderCallbacks(FragmentActivity activity, 
			ArbiterAdapter<ArrayList<Layer>> adapter, int loaderId){
		this.layerAdapter = adapter;
		this.activity = activity;
		
		activity.getSupportLoaderManager().initLoader(loaderId, null, this);
	}
	
	@Override
	public Loader<ArrayList<Layer>> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new LayersListLoader(activity);
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<Layer>> loader, ArrayList<Layer> data) {
		layerAdapter.setData(data);
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<Layer>> loader) {
		layerAdapter.setData(null);
	}	
}
