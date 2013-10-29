package com.lmn.Arbiter_Android.Activities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.Map;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.LoaderCallbacks.MapLoaderCallbacks;
import com.lmn.Arbiter_Android.ListAdapters.LayerListAdapter;

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

public class MapActivity extends FragmentActivity implements CordovaInterface, LayerListAdapter.LayerChangeListener{
    private ArbiterDialogs dialogs;
    private boolean welcomed;
    private String TAG = "MAP_ACTIVITY";
    private ArbiterProject arbiterProject;
	private MapLoaderCallbacks mapLoaderCallbacks;
    private static final String cordovaUrl = "file:///android_asset/www/index.html";
    
    // For CORDOVA
    private CordovaWebView cordovaWebview;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Config.init(this);
        
        Init(savedInstanceState);
        
        dialogs = new ArbiterDialogs(getResources(), getSupportFragmentManager());

        cordovaWebview = (CordovaWebView) findViewById(R.id.webView1);
        
        cordovaWebview.loadUrl(cordovaUrl, 5000);
    }

    public void Init(Bundle savedInstanceState){
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
    			
    			cordovaWebview.loadUrl("javascript:app.zoomToAOI(" + aoi + ")");
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
        	
        	case R.id.action_make_available_offline:
        		
        		return true;
        		
        	case R.id.action_settings:
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
    	Log.d(TAG, "onResume");
    	if((arbiterProject != null) && !arbiterProject.isSameProject() && (this.mapLoaderCallbacks != null)){
    		this.mapLoaderCallbacks.loadMap();
    		arbiterProject.makeSameProject();
    	}
    }
    
    @Override
    protected void onDestroy(){
    	super.onDestroy();
    	if(this.cordovaWebview != null){
    		cordovaWebview.handleDestroy();
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
	public void onLayerDeleted(long layerId) {
		Map.getMap().deleteLayer(cordovaWebview, layerId);
	}

	@Override
	public void onLayerVisibilityChanged(long layerId) {
		
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
        if (message.equalsIgnoreCase("exit")) {
                super.finish();
        }else if(message.equals("onPageFinished")){
        	if(obj instanceof String && ((String) obj).equals(cordovaUrl)){
        		this.mapLoaderCallbacks = new MapLoaderCallbacks(this, cordovaWebview , R.id.loader_map);
                this.arbiterProject.makeSameProject();	
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

