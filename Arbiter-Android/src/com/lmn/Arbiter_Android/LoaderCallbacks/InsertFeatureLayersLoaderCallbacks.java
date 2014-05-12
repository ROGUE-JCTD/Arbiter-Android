package com.lmn.Arbiter_Android.LoaderCallbacks;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.ListAdapters.ArbiterAdapter;
import com.lmn.Arbiter_Android.Loaders.InsertFeaturesLayersLoader;

public class InsertFeatureLayersLoaderCallbacks implements LoaderManager.LoaderCallbacks<ArrayList<Layer>>{

	private ArbiterAdapter<ArrayList<Layer>> layerAdapter;
	private FragmentActivity activity;
	
	public InsertFeatureLayersLoaderCallbacks(FragmentActivity activity, 
			ArbiterAdapter<ArrayList<Layer>> adapter, int loaderId){
		this.layerAdapter = adapter;
		this.activity = activity;
		
		activity.getSupportLoaderManager().initLoader(loaderId, null, this);
	}
	
	@Override
	public Loader<ArrayList<Layer>> onCreateLoader(int id, Bundle bundle) {
        return new InsertFeaturesLayersLoader(getActivity());
	}

	protected FragmentActivity getActivity(){
		return activity;
	}
	
	@Override
	public void onLoadFinished(Loader<ArrayList<Layer>> loader, ArrayList<Layer> data) {
		Log.w("LayerLoaderCallbacks", "LayerLoaderCallbacks data length = " + data.size());
		layerAdapter.setData(data);
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<Layer>> loader) {
		layerAdapter.setData(null);
	}	
}
