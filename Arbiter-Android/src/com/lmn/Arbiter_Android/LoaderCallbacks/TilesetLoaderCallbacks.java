package com.lmn.Arbiter_Android.LoaderCallbacks;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.ListAdapters.ArbiterAdapter;
import com.lmn.Arbiter_Android.Loaders.TilesetsListLoader;

import java.util.ArrayList;

public class TilesetLoaderCallbacks implements LoaderManager.LoaderCallbacks<ArrayList<Tileset>>{

	private ArbiterAdapter<ArrayList<Tileset>> tilesetAdapter;
	private FragmentActivity activity;

	public TilesetLoaderCallbacks(FragmentActivity activity,
								  ArbiterAdapter<ArrayList<Tileset>> adapter, int loaderId){
		this.tilesetAdapter = adapter;
		this.activity = activity;
		
		activity.getSupportLoaderManager().initLoader(loaderId, null, this);
	}
	
	@Override
	public Loader<ArrayList<Tileset>> onCreateLoader(int id, Bundle bundle) {
        return new TilesetsListLoader(getActivity());
	}

	protected FragmentActivity getActivity(){
		return activity;
	}
	
	@Override
	public void onLoadFinished(Loader<ArrayList<Tileset>> loader, ArrayList<Tileset> data) {
		Log.w("TilesetLoaderCallbacks", "TilesetLoaderCallbacks data length = " + data.size());
		tilesetAdapter.setData(data);
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<Tileset>> loader) {
		tilesetAdapter.setData(null);
	}	
}
