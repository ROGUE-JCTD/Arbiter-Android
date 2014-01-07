package com.lmn.Arbiter_Android.Activities;

import java.util.Locale;
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
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.Dialog.Dialogs.InsertFeatureDialog;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

import android.os.Bundle;
import android.app.Activity;
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
import android.widget.RelativeLayout;

public class MapActivity extends FragmentActivity implements CordovaInterface, Map.MapChangeListener, Map.CordovaMap{
    private ArbiterDialogs dialogs;
    private String TAG = "MAP_ACTIVITY";
    private ArbiterProject arbiterProject;
    private InsertProjectHelper insertHelper;
    private MapChangeHelper mapChangeHelper;
    private IncompleteProjectHelper incompleteProjectHelper;
    private boolean menuPrepared;
    
    // For CORDOVA
    private CordovaWebView cordovaWebView;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private CordovaPlugin activityResultCallback;
    protected boolean activityResultKeepRunning;
    
    // Keep app running when pause is received. (default = true)
    // If true, then the JavaScript and native code continue to run in the background
    // when another application (activity) is started.
    protected boolean keepRunning = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Config.init(this);
        
        Init(savedInstanceState);
        
        this.keepRunning = this.getBooleanProperty("KeepRunning", true);
        
        dialogs = new ArbiterDialogs(getApplicationContext(), getResources(), getSupportFragmentManager());

        cordovaWebView = (CordovaWebView) findViewById(R.id.webView1);
        
        cordovaWebView.loadUrl(ArbiterCordova.mainUrl, 5000);
        
        mapChangeHelper = new MapChangeHelper(this, 
        		cordovaWebView, incompleteProjectHelper);
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
    	
    	ImageButton syncButton = (ImageButton) findViewById(R.id.syncButton);
    	
    	final String title = activity.getResources().getString(R.string.sync_in_progress);
		final String message = activity.getResources().getString(R.string.sync_in_progress_msg);
		
    	syncButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			arbiterProject.showSyncProgressDialog(activity, title, message);
    			
    			Map.getMap().sync(cordovaWebView);
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
    			
    			helper.displayFeatureDialog(selectedFeature.getFeatureType(),
    					selectedFeature.getId(), ArbiterState.getArbiterState()
    					.getLayerBeingEdited());
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
    	
    	ImageButton locationButton = (ImageButton) findViewById(R.id.locationButton);
    	
    	locationButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			Map.getMap().zoomToCurrentPosition(cordovaWebView);
    		}
    	});
    	
    	RelativeLayout incompleteBar = (RelativeLayout) findViewById(R.id.incompleteProjectBar);
    	
    	incompleteBar.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			startAOIActivity();
    		}
    	});	
    	
    	initIncompleteProjectHelper();
    	
    	incompleteProjectHelper.setSyncButton(syncButton);
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
    
    private void initIncompleteProjectHelper(){
    	if(incompleteProjectHelper == null){
    		incompleteProjectHelper = new IncompleteProjectHelper(this);
    	}
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
    	if(!this.menuPrepared){
    		initIncompleteProjectHelper();
    		
    		incompleteProjectHelper.setInsertButton(menu);
        	
        	this.menuPrepared = true;
    	}
    	
    	return true;
    }
    
    private void openInsertFeatureDialog(){
    	String title = getResources().getString(R.string.insert_feature_title);
    	String cancel = getResources().getString(android.R.string.cancel);
    	
    	DialogFragment frag = InsertFeatureDialog.newInstance(title, cancel);
    	
    	frag.show(getSupportFragmentManager(), InsertFeatureDialog.TAG);
    }
    
    private void startAOIActivity(){
    	Intent aoiIntent = new Intent(this, AOIActivity.class);
		this.startActivity(aoiIntent);
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
        		startAOIActivity();
        		
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

        if (this.cordovaWebView == null) {
            return;
        } else if(this.isFinishing()){
            this.cordovaWebView.handlePause(this.keepRunning);
        }
    }
    
    @Override 
    protected void onResume(){
    	super.onResume();
    	Log.w(TAG, TAG + " onResume");
    	
    	if (this.cordovaWebView == null) {
    		return;
        }

        this.cordovaWebView.handleResume(this.keepRunning,
        	this.activityResultKeepRunning);

        // If app doesn't want to run in background
        if (!this.keepRunning || this.activityResultKeepRunning) {

        	// Restore multitasking state
            if (this.activityResultKeepRunning) {
                this.keepRunning = this.activityResultKeepRunning;
                this.activityResultKeepRunning = false;
            }
        }
         
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
        			
        			// If the user changed projects, check to 
        			// see if the project has an aoi or not
        			incompleteProjectHelper.checkForAOI();
        		}
    		}
    	}
    }
    
    private void updateProjectAOI(){
    	final String aoi = ArbiterState.getArbiterState().getNewAOI();
		
    	updateProjectAOI(aoi);
    }
    
    private void updateProjectAOI(String aoi){
    	String title = getResources().getString(R.string.sync_in_progress);
		String message = getResources().getString(R.string.sync_in_progress_msg);
		
    	arbiterProject.showSyncProgressDialog(this, title, message);
    	
        Map.getMap().updateAOI(cordovaWebView, aoi);
        
        // Set the new aoi to null so the we know we're
        // not setting the aoi anymore.
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
	public void setActivityResultCallback(CordovaPlugin plugin) {
		this.activityResultCallback = plugin; 
	}

	@Override
	public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
		this.activityResultCallback = command;
	    this.activityResultKeepRunning = this.keepRunning;

	    // If multitasking turned on, then disable it for activities that return results
	    if (command != null) {
	        this.keepRunning = false;
	    }

	    // Start activity
	    super.startActivityForResult(intent, requestCode);
	}
	
	/**
	 * Called when an activity you launched exits, giving you the requestCode you started it with,
	 * the resultCode it returned, and any additional data from it.
	 *
	 * @param requestCode       The request code originally supplied to startActivityForResult(),
	 *                          allowing you to identify who this result came from.
	 * @param resultCode        The integer result code returned by the child activity through its setResult().
	 * @param data              An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    super.onActivityResult(requestCode, resultCode, intent);
	    CordovaPlugin callback = this.activityResultCallback;
	    if (callback != null) {
	        callback.onActivityResult(requestCode, resultCode, intent);
	    }
	}
	
	/**
     * Get boolean property for activity.
     *
     * @param name
     * @param defaultValue
     * @return the boolean value of the named property
     */
    public boolean getBooleanProperty(String name, boolean defaultValue) {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null) {
            return defaultValue;
        }
        name = name.toLowerCase(Locale.getDefault());
        Boolean p;
        try {
            p = (Boolean) bundle.get(name);
        } catch (ClassCastException e) {
            String s = bundle.get(name).toString();
            if ("true".equals(s)) {
                p = true;
            }
            else {
                p = false;
            }
        }
        if (p == null) {
            return defaultValue;
        }
        return p.booleanValue();
    }
}

