package com.lmn.Arbiter_Android.LoaderCallbacks;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;

import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.ListAdapters.ArbiterAdapter;
import com.lmn.Arbiter_Android.Loaders.ChooseBaseLayerLoader;

public class ChooseBaseLayerLoaderCallbacks extends LayerLoaderCallbacks {
	
	public ChooseBaseLayerLoaderCallbacks(FragmentActivity activity,
			ArbiterAdapter<ArrayList<Layer>> adapter, int loaderId) {
		super(activity, adapter, loaderId);
	}

	@Override
	public Loader<ArrayList<Layer>> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new ChooseBaseLayerLoader(getActivity());
	}
}
