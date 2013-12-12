package com.lmn.Arbiter_Android.Activities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.ArbiterState;
import com.lmn.Arbiter_Android.InsertProjectHelper;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.CordovaPlugins.ArbiterCordova;
import com.lmn.Arbiter_Android.CordovaPlugins.Helpers.FeatureHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.Dialog.Dialogs.InsertFeatureDialog;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class MapActivity extends FragmentActivity implements CordovaInterface, Map.MapChangeListener, Map.CordovaMap{
    private ArbiterDialogs dialogs;
    private String TAG = "MAP_ACTIVITY";
    private ArbiterProject arbiterProject;
    private InsertProjectHelper insertHelper;
    private MapChangeHelper mapChangeHelper;
    
    // For CORDOVA
    private CordovaWebView cordovaWebView;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Config.init(this);
        
        Init(savedInstanceState);
        
        dialogs = new ArbiterDialogs(getApplicationContext(), getResources(), getSupportFragmentManager());

        cordovaWebView = (CordovaWebView) findViewById(R.id.webView1);
        
        cordovaWebView.loadUrl(ArbiterCordova.mainUrl, 5000);
        
        mapChangeHelper = new MapChangeHelper(this, cordovaWebView);
    }

    private void Init(Bundle savedInstanceState){
    	getProjectStructure();
    	InitApplicationDatabase();
        InitArbiterProject();
        setListeners();
    }
    
    private void InitApplicationDatabase(){
    	ApplicationDatabaseHelper.
    		getHelper(getApplicationContext());
    }
    
    private void getProjectStructure(){
    	ProjectStructure.getProjectStructure();
    }
    
    private void InitArbiterProject(){
    	arbiterProject = ArbiterProject.getArbiterProject();
    	
    	// This will also ensure that a project exists
    	arbiterProject.getOpenProject(this);
    }
    
    private void resetSavedExtent(){
        arbiterProject.setSavedBounds(null);
        arbiterProject.setSavedZoomLevel(null);
    }
    
    /**
     * Set listeners
     */
    private void setListeners(){
    	final MapActivity activity = this;
    	
    	ImageButton layersButton = (ImageButton) findViewById(R.id.layersButton);
    	
    	layersButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			dialogs.showLayersDialog();
    		}
    	});
    	
    	ImageButton aoiButton = (ImageButton) findViewById(R.id.AOIButton);
    	
    	aoiButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			Map.getMap().zoomToAOI(cordovaWebView);
    		}
    	});
    	
    	ImageButton cancelEditing = (ImageButton) findViewById(R.id.cancelButton);
    	
    	cancelEditing.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			// Toggle the cancel and done buttons
    			mapChangeHelper.doneEditingFeature();
    			
    			// Exit modify mode
    			Map.getMap().cancelEdit(cordovaWebView);
    			
    			// Open up the feature dialog for the selectedFeature
    			Feature selectedFeature = ArbiterState.getArbiterState().isEditingFeature();
    			
    			FeatureHelper helper = new FeatureHelper(activity);
    			
    			helper.displayFeatureDialog(selectedFeature
    					.getFeatureType(), selectedFeature.getId());
    		}
    	});
    	
    	ImageButton doneEditing = (ImageButton) findViewById(R.id.doneButton);
    	
    	doneEditing.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			Map.getMap().getUpdatedGeometry(cordovaWebView);
    		}
    	});
    	
    	ImageButton cancelInsert = (ImageButton) findViewById(R.id.cancelInsertButton);
    	
    	cancelInsert.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			mapChangeHelper.endInsertMode();
    		}
    	});
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }
    
    private void openInsertFeatureDialog(){
    	String title = getResources().getString(R.string.insert_feature_title);
    	String cancel = getResources().getString(android.R.string.cancel);
    	
    	DialogFragment frag = InsertFeatureDialog.newInstance(title, cancel);
    	
    	frag.show(getSupportFragmentManager(), InsertFeatureDialog.TAG);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()) {
    		case R.id.action_new_feature:
    			openInsertFeatureDialog();
    			
    			return true;
        	
    		case R.id.action_servers:
        		dialogs.showServersDialog();
        		return true;
        		
        	case R.id.action_projects:
        		Intent projectsIntent = new Intent(this, ProjectsActivity.class);
        		this.startActivity(projectsIntent);
        		
        		return true;
        	
        	case R.id.action_aoi:
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

    @Override
    protected void onPause() {
    	super.onPause();
        Log.d(TAG, "onPause");  
    }
    
    @Override 
    protected void onResume(){
    	super.onResume();
    	Log.w(TAG, TAG + " onResume");
    	
    	if(arbiterProject != null){
    		resetSavedExtent();
    		
    		if(ArbiterState.getArbiterState().isCreatingProject()){
    			arbiterProject.showCreateProjectProgress(
    					this, 
    					getResources().getString(R.string.create_project_progress),
    					getResources().getString(R.string.create_project_msg)
    			);
    			
    			insertHelper = new InsertProjectHelper(this);
    			insertHelper.insert();
    		}else if(ArbiterState.getArbiterState().isSettingAOI()){
    			updateProjectAOI();
    		}else{
    			if(!arbiterProject.isSameProject(getApplicationContext())){
        			Map.getMap().resetWebApp(cordovaWebView);
        			arbiterProject.makeSameProject();
        		}
    		}
    	}
    }
    
    private void updateProjectAOI(){
    	final String aoi = ArbiterState.getArbiterState().getNewAOI();
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				updateProjectAOI(aoi);
			}
		});
    }
    
    private void updateProjectAOI(String aoi){
    	Context context = getApplicationContext();
		String projectName = ArbiterProject.getArbiterProject().getOpenProject(this);
		
		ProjectDatabaseHelper helper = 
				ProjectDatabaseHelper.getHelper(context, 
						ProjectStructure.getProjectPath(
								context, projectName), false);
		
        PreferencesHelper.getHelper().update(helper.getWritableDatabase(),
        		context, ArbiterProject.AOI, aoi);
        
        ArbiterState.getArbiterState().setNewAOI(null);
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
     * Map.MapChangeListener methods
     */
    public MapChangeHelper getMapChangeHelper(){
    	return this.mapChangeHelper;
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
        		if(((String) obj).equals(ArbiterCordova.mainUrl)){
        			//if(ArbiterState.getState().isCreatingProject()){
        			//	insertHelper.performFeatureDbWork();
        			//}else{
        				String savedBounds = arbiterProject.getSavedBounds();
            			String savedZoom = arbiterProject.getSavedZoomLevel();
            			
            			if(savedBounds != null && savedZoom != null){
            				Map.getMap().zoomToExtent(cordovaWebView, 
                					arbiterProject.getSavedBounds(),
                					arbiterProject.getSavedZoomLevel());
            			}else{
            				Map.getMap().zoomToAOI(cordovaWebView);
            			}
            			
                        this.arbiterProject.makeSameProject();
        			//}
        		}else if(((String) obj).equals("about:blank")){
        			this.cordovaWebView.loadUrl(ArbiterCordova.mainUrl);
        		}
        		
        		this.cordovaWebView.clearHistory();
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

