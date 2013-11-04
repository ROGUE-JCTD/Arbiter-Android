package com.lmn.Arbiter_Android.LoaderCallbacks;

import java.util.ArrayList;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;
import com.lmn.Arbiter_Android.Loaders.MapLoader;
import com.lmn.Arbiter_Android.Map.Map;

public class MapLoaderCallbacks implements LoaderManager.LoaderCallbacks<ArrayList<Layer>>{

	private FragmentActivity activity;
	private CordovaWebView webview;
	private Loader<ArrayList<Layer>> loader;
	private CordovaInterface cordovaListener;
	
	public MapLoaderCallbacks(FragmentActivity activity, CordovaWebView webview, int loaderId){
		this.activity = activity;
		this.webview = webview;
		
		try {
			cordovaListener = (CordovaInterface) activity;
		} catch (ClassCastException e){
			e.printStackTrace();
			throw new ClassCastException(activity.toString() 
					+ " must implement CordovaInterface");
		}
		
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
	public void onLoadFinished(Loader<ArrayList<Layer>> loader, final ArrayList<Layer> layers) {
		final ArbiterProject arbiterProject = ArbiterProject.getArbiterProject();
		final Context context = activity.getApplicationContext();
		final FragmentActivity activity = this.activity;
		
		cordovaListener.getThreadPool().execute(new Runnable(){
			@Override
			public void run(){
				
				String savedBounds = arbiterProject.getSavedBounds();
				final String savedZoomLevel = arbiterProject.getSavedZoomLevel();
				
				if(savedBounds == null){
					Log.w("MapLoaderCallbacks", "onLoadFinished savedBounds were null");
					GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(context);
					savedBounds = ProjectsHelper.getProjectsHelper().getProjectAOI(
							helper.getWritableDatabase(), context, 
							ArbiterProject.getArbiterProject().getOpenProject(context));
				}
				
				final String extent = savedBounds;
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						Map.getMap().loadMap(webview, layers, 
								arbiterProject.includeDefaultLayer());
						
						Map.getMap().zoomToExtent(webview, extent, savedZoomLevel);
					}
				});
			}
		});
	}
	
	@Override
	public void onLoaderReset(Loader<ArrayList<Layer>> loader) {
		Log.w("MapLoaderCallbacks", "MapLoaderCallbacks: onLoaderReset");
		Map.getMap().loadMap(webview, null, 
				ArbiterProject.getArbiterProject().includeDefaultLayer());
	}	
	
	public void loadMap(){
		loader.onContentChanged();
	}
}
