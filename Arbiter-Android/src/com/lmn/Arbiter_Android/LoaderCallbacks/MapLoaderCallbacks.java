package com.lmn.Arbiter_Android.LoaderCallbacks;

import org.apache.cordova.CordovaWebView;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.lmn.Arbiter_Android.Map;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.Loaders.MapLoader;

public class MapLoaderCallbacks implements LoaderManager.LoaderCallbacks<Layer[]>{

	private FragmentActivity activity;
	private Map map;
	private Loader<Layer[]> loader;
	
	public MapLoaderCallbacks(FragmentActivity activity, CordovaWebView webview, int loaderId){
		this.activity = activity;
		this.map = new Map(webview);
		activity.getSupportLoaderManager().initLoader(loaderId, null, this);
	}
	
	@Override
	public Loader<Layer[]> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
		this.loader = new MapLoader(activity.getApplicationContext());
        return this.loader;
	}

	@Override
	public void onLoadFinished(Loader<Layer[]> loader, Layer[] layers) {
		map.loadMap(layers);
	}

	@Override
	public void onLoaderReset(Loader<Layer[]> loader) {
		map.loadMap(null);
	}	
	
	public void loadMap(){
		loader.onContentChanged();
	}
}
