package com.lmn.Arbiter_Android.LoaderCallbacks;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.Dialog.Dialogs.AddLayersDialog;
import com.lmn.Arbiter_Android.Loaders.AddLayersListLoader;
import com.lmn.Arbiter_Android.ListAdapters.AddLayersListAdapter;

public class AddLayersLoaderCallbacks implements LoaderManager.LoaderCallbacks<ArrayList<Layer>>{
	private AddLayersListAdapter addLayersAdapter;
	private AddLayersDialog fragment;
	
	public AddLayersLoaderCallbacks(AddLayersDialog fragment, AddLayersListAdapter addLayersAdapter, int id){
		this.addLayersAdapter = addLayersAdapter;
		this.fragment = fragment;
		
		fragment.getActivity().getSupportLoaderManager().initLoader(id, null, this);
	}
	
	@Override
	public Loader<ArrayList<Layer>> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new AddLayersListLoader(fragment);
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<Layer>> loader, ArrayList<Layer> data) {
		if(data == null){
			data = new ArrayList<Layer>();
		}
		
		addLayersAdapter.setData(data);
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<Layer>> loader) {
		addLayersAdapter.setData(null);
	}
}
