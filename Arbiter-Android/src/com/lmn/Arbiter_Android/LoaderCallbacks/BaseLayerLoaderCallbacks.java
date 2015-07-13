package com.lmn.Arbiter_Android.LoaderCallbacks;

import org.json.JSONArray;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.lmn.Arbiter_Android.ListAdapters.ArbiterAdapter;
import com.lmn.Arbiter_Android.Loaders.BaseLayerLoader;

public class BaseLayerLoaderCallbacks implements LoaderManager.LoaderCallbacks<JSONArray>{

	private ArbiterAdapter<JSONArray> baseLayerAdapter;
	
	private DialogFragment fragment;
	
	public BaseLayerLoaderCallbacks(DialogFragment fragment, ArbiterAdapter<JSONArray> adapter, int id){
		this.baseLayerAdapter = adapter;
		this.fragment = fragment;
		
		fragment.getActivity().getSupportLoaderManager().initLoader(id, null, this);
	}
	
	@Override
	public Loader<JSONArray> onCreateLoader(int id, Bundle bundle) {
        return new BaseLayerLoader(this.fragment.getActivity());
	}

	@Override
	public void onLoadFinished(Loader<JSONArray> loader, JSONArray data) {
		Log.w("BaseLayerLoaderCallback", "BaseLayerLoaderCallbacks.onLoadFinished");
		baseLayerAdapter.setData(data);
	}

	@Override
	public void onLoaderReset(Loader<JSONArray> loader) {
		baseLayerAdapter.setData(null);
	}	
}
