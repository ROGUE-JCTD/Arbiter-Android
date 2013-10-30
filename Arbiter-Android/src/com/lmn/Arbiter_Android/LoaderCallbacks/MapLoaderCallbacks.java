package com.lmn.Arbiter_Android.LoaderCallbacks;

import java.util.ArrayList;

import org.apache.cordova.CordovaWebView;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.Loaders.MapLoader;
import com.lmn.Arbiter_Android.Map.Map;

public class MapLoaderCallbacks implements LoaderManager.LoaderCallbacks<ArrayList<Layer>>{

	private FragmentActivity activity;
	private CordovaWebView webview;
	private Loader<ArrayList<Layer>> loader;
	
	public MapLoaderCallbacks(FragmentActivity activity, CordovaWebView webview, int loaderId){
		this.activity = activity;
		this.webview = webview;
		activity.getSupportLoaderManager().initLoader(loaderId, null, this);
	}
	
	@Override
	public Loader<ArrayList<Layer>> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
		this.loader = new MapLoader(activity.getApplicationContext());
        return this.loader;
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<Layer>> loader, ArrayList<Layer> layers) {
		Map.getMap().loadMap(webview, layers, 
				ArbiterProject.getArbiterProject().includeDefaultLayer());
	}
	
	@Override
	public void onLoaderReset(Loader<ArrayList<Layer>> loader) {
		Map.getMap().loadMap(webview, null, 
				ArbiterProject.getArbiterProject().includeDefaultLayer());
	}	
	
	public void loadMap(){
		loader.onContentChanged();
	}
}
