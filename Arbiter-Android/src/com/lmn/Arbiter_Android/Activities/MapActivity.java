package com.lmn.Arbiter_Android.Activities;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.CordovaPlugins.ArbiterCordova;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.LoaderCallbacks.MapLoaderCallbacks;
import com.lmn.Arbiter_Android.Map.Map;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ToggleButton;

public class MapActivity extends FragmentActivity implements CordovaInterface, Map.MapChangeListener, Map.CordovaMap{
    private ArbiterDialogs dialogs;
    private boolean welcomed;
    private String TAG = "MAP_ACTIVITY";
    private ArbiterProject arbiterProject;
	private MapLoaderCallbacks mapLoaderCallbacks;
    
    // For CORDOVA
    private CordovaWebView cordovaWebView;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Config.init(this);
        
        Init(savedInstanceState);
        
        dialogs = new ArbiterDialogs(getResources(), getSupportFragmentManager());

        cordovaWebView = (CordovaWebView) findViewById(R.id.webView1);
        
        cordovaWebView.loadUrl(ArbiterCordova.cordovaUrl, 5000);
    }

    private void Init(Bundle savedInstanceState){
    	restoreState(savedInstanceState);
        InitDatabases();
        InitArbiterProject();
        setListeners();
    }
    
    /**
     * Make sure that the database gets initialized and a project exists
     */
    private void InitDatabases(){
    	GlobalDatabaseHelper
    			.getGlobalHelper(getApplicationContext());
    }
    
    private void InitArbiterProject(){
    	arbiterProject = ArbiterProject.getArbiterProject();
    	arbiterProject.getOpenProject(getApplicationContext());
    }
    
    private void resetSavedExtent(){
    	arbiterProject.setSavedBounds(null);
    	arbiterProject.setSavedZoomLevel(null);
    }
    
    /**
     * Set listeners
     */
    private void setListeners(){
    	ImageButton layersButton = (ImageButton) findViewById(R.id.layersButton);
    	
    	layersButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			dialogs.showLayersDialog();
    		}
    	});
    	
    	ImageButton aoiButton = (ImageButton) findViewById(R.id.AOIButton);
    	
    	final Context context = this.getApplicationContext();
    	
    	aoiButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(context);
    			String aoi = ProjectsHelper.getProjectsHelper().getProjectAOI(
    					helper.getWritableDatabase(), context, 
    					ArbiterProject.getArbiterProject().getOpenProject(context));
    			
    			Log.w(TAG, "aoi: " + aoi);
    			Map.getMap().zoomToExtent(cordovaWebView, aoi, null);
    		}
    	});
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState){
    	saveState(outState);
    	super.onSaveInstanceState(outState);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()) {
    		case R.id.action_new_feature:
    			//menuEvents.activateAddFeatures();;
    			return true;
        	
    		case R.id.action_servers:
        		dialogs.showServersDialog();
        		return true;
        		
        	case R.id.action_projects:
        		Intent projectsIntent = new Intent(this, ProjectsActivity.class);
        		this.startActivity(projectsIntent);
        		
        		return true;
        	
        	case R.id.action_aoi:
        		// Make the app aware that the aoi is being changed
        		ArbiterProject.getArbiterProject().isSettingAOI(true);
        		
        		Intent aoiIntent = new Intent(this, AOIActivity.class);
        		this.startActivity(aoiIntent);
        		
        		return true;
        		
        	case R.id.action_language:
        		//menuEvents.showSettings(this);
        		return true;
    		
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    public void saveState(Bundle outState){
    	outState.putBoolean("welcomed", welcomed);
    }
    
    public void restoreState(Bundle savedInstanceState){
    	if(savedInstanceState != null){
    		welcomed = savedInstanceState.getBoolean("welcomed");
    	}
    }
    
    public void displayWelcomeDialog(){
    	dialogs.showWelcomeDialog();
    }
    
    public void toggleLayerVisibility(View view){
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {

		} else {

		}
	}

    @Override
    protected void onPause() {
    	super.onPause();
        Log.d(TAG, "onPause");  
    }
    
    @Override 
    protected void onResume(){
    	super.onResume();
    	
    	// Reset the saved extent and zoom level
    	resetSavedExtent();
    	
    	if((arbiterProject != null) && !arbiterProject.isSameProject() && (this.mapLoaderCallbacks != null)){
    		this.mapLoaderCallbacks.loadMap();
    		arbiterProject.makeSameProject();
    	}
    }
    
    @Override
    protected void onDestroy(){
    	super.onDestroy();
    	if(this.cordovaWebView != null){
    		cordovaWebView.handleDestroy();
    	}
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) 
    {
        super.onConfigurationChanged(newConfig);
    }
    
    /**
	 * LayerChangeListener events
	 */
	@Override
	public void onLayerDeleted(final long layerId) {
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Map.getMap().deleteLayer(cordovaWebView, layerId);
			}
		});
	}

	@Override
	public void onLayerVisibilityChanged(final long layerId) {
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Map.getMap().toggleLayerVisibility(cordovaWebView, layerId);
			}
		});
	}
	
	@Override
	public void onLayersAdded(final ArrayList<Layer> layers, final long[] layerIds,
			final boolean includeDefaultLayer, final boolean defaultLayerVisibility) {
		
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Map.getMap().addLayers(cordovaWebView, 
						layers, layerIds, includeDefaultLayer, defaultLayerVisibility);
			}
		});
	}
	
	/**
	 * Map.CordovaMap methods
	 */
	@Override
	public CordovaWebView getWebView(){
		return this.cordovaWebView;
	}
	
    /**
     * Cordova methods
     */
	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public ExecutorService getThreadPool() {
		return threadPool;
	}

	@Override
	public Object onMessage(String message, Object obj) {
		Log.d(TAG, message);
        if(message.equals("onPageFinished")){
        	if(obj instanceof String){
        		if(((String) obj).equals(ArbiterCordova.cordovaUrl)){
        			if(this.mapLoaderCallbacks == null){
        				this.mapLoaderCallbacks = new MapLoaderCallbacks(this, cordovaWebView , R.id.loader_map);
        			}else{
        				this.mapLoaderCallbacks.loadMap();
        			}
        			
                    this.arbiterProject.makeSameProject();
        		}else if(((String) obj).equals("about:blank")){
        			this.cordovaWebView.loadUrl(ArbiterCordova.cordovaUrl);
        		}
        	}
        }
        return null;
	}
	
	@Override
	public void setActivityResultCallback(CordovaPlugin cordovaPlugin) {
		Log.d(TAG, "setActivityResultCallback is unimplemented");
		
	}

	@Override
	public void startActivityForResult(CordovaPlugin cordovaPlugin, Intent intent, int resultCode) {
		Log.d(TAG, "startActivityForResult is unimplemented");
		
	}
}

