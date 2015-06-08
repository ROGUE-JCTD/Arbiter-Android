package com.lmn.Arbiter_Android.LoaderCallbacks;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.Dialog.Dialogs.AddTilesetDialog;
import com.lmn.Arbiter_Android.ListAdapters.AddTilesetsListAdapter;
import com.lmn.Arbiter_Android.Loaders.AddTilesetsListLoader;

import java.util.ArrayList;

public class AddTilesetsLoaderCallbacks implements LoaderManager.LoaderCallbacks<ArrayList<Tileset>>{
	private AddTilesetsListAdapter addTilesetsAdapter;
	private AddTilesetDialog fragment;

	public AddTilesetsLoaderCallbacks(AddTilesetDialog fragment, AddTilesetsListAdapter addTilesetsAdapter, int id){
		this.addTilesetsAdapter = addTilesetsAdapter;
		this.fragment = fragment;
		
		fragment.getActivity().getSupportLoaderManager().initLoader(id, null, this);
	}
	
	@Override
	public Loader<ArrayList<Tileset>> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new AddTilesetsListLoader(fragment);
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<Tileset>> loader, ArrayList<Tileset> data) {
		if(data == null){
			data = new ArrayList<Tileset>();
		}
		
		addTilesetsAdapter.setData(data);
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<Tileset>> loader) {
		addTilesetsAdapter.setData(null);
	}
}
