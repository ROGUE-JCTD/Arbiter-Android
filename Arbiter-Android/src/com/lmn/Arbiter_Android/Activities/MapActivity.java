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
import com.lmn.Arbiter_Android.OOMWorkaround;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.About.About;
import com.lmn.Arbiter_Android.ConnectivityListeners.ConnectivityListener;
import com.lmn.Arbiter_Android.ConnectivityListeners.CookieConnectivityListener;
import com.lmn.Arbiter_Android.ConnectivityListeners.HasConnectivityListener;
import com.lmn.Arbiter_Android.ConnectivityListeners.SyncConnectivityListener;
import com.lmn.Arbiter_Android.CookieManager.ArbiterCookieManager;
import com.lmn.Arbiter_Android.CordovaPlugins.ArbiterCordova;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ControlPanelHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.SyncTableHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.Dialog.Dialogs.FailedSyncHelper;
import com.lmn.Arbiter_Android.Dialog.Dialogs.InsertFeatureDialog;
import com.lmn.Arbiter_Android.Dialog.ProgressDialog.SyncProgressDialog;
import com.lmn.Arbiter_Android.GeometryEditor.GeometryEditor;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.Notifications.Sync;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;
import com.lmn.Arbiter_Android.ReturnQueues.OnReturnToMap;
import com.lmn.Arbiter_Android.Settings.Settings;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

public class MapActivity extends FragmentActivity implements CordovaInterface,
		Map.MapChangeListener, Map.CordovaMap, HasThreadPool, HasConnectivityListener{
	
    private ArbiterDialogs dialogs;
    private String TAG = "MAP_ACTIVITY";
    private ArbiterProject arbiterProject;
    private MapChangeHelper mapChangeHelper;
    private IncompleteProjectHelper incompleteProjectHelper;
    private boolean menuPrepared;
	private SyncConnectivityListener syncConnectivityListener;
    private CookieConnectivityListener cookieConnectivityListener;
    private NotificationBadge notificationBadge;
    private boolean isDestroyed = false;
    // For CORDOVA
    private CordovaWebView cordovaWebView;
    
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private CordovaPlugin activityResultCallback;
    protected boolean activityResultKeepRunning;
    
    // Keep app running when pause is received. (default = true)
    // If true, then the JavaScript and native code continue to run in the background
    // when another application (activity) is started.
    protected boolean keepRunning = true;
    
    private FailedSyncHelper failedSyncHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Config.init(this);
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        
        Init(savedInstanceState);
        
        this.keepRunning = this.getBooleanProperty("KeepRunning", true);
        
        dialogs = new ArbiterDialogs(getApplicationContext(), getResources(), getSupportFragmentManager());
        
        cordovaWebView = (CordovaWebView) findViewById(R.id.webView1);
        
        cordovaWebView.loadUrl(ArbiterCordova.mainUrl, 5000);
        
        mapChangeHelper = new MapChangeHelper(this, 
        		cordovaWebView, incompleteProjectHelper);
        
        checkNotificationsAreComputed();
    }
    
    private String getProjectPath(){
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(this);
		
		return ProjectStructure.getProjectPath(projectName);
	}
	
	private SQLiteDatabase getProjectDatabase(){
		return ProjectDatabaseHelper.getHelper(getApplicationContext(),
				getProjectPath(), false).getWritableDatabase();
	}
	
    private void Init(Bundle savedInstanceState){
    	getProjectStructure();
    	InitApplicationDatabase();
        InitArbiterProject();
        setListeners();
        clearControlPanelKVP();
        clearFindMe();
        
        this.failedSyncHelper = new FailedSyncHelper(this, 
        		getProjectDatabase(), this.syncConnectivityListener, this);
        
        this.failedSyncHelper.checkIncompleteSync();
    }
    
    private void clearFindMe(){
    	
    	SQLiteDatabase projectDb = (new Util()).getProjectDb(this, false);
    	
    	PreferencesHelper.getHelper().delete(projectDb, getApplicationContext(), PreferencesHelper.FINDME);
    }
    
    private void clearControlPanelKVP(){
		ControlPanelHelper helper = new ControlPanelHelper(this);
    				
    	helper.clearControlPanel();
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
    
    /**
     * Set listeners
     */
    private void setListeners(){
    	final MapActivity activity = this;
    	
    	ImageButton layersButton = (ImageButton) findViewById(R.id.layersButton);
    	
    	layersButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			if(arbiterProject != null) {
	    			String openProject = arbiterProject.getOpenProject(activity);
	            	if(openProject.equals(activity.getResources().getString(R.string.default_project_name))) {
	            		// create new project
	            		Map.getMap().createNewProject(cordovaWebView);
	            	} else {
	        			dialogs.showLayersDialog(activity);
	            	}
    			}
    		}
    	});
    	
    	ImageButton syncButton = (ImageButton) findViewById(R.id.syncButton);
		
    	syncConnectivityListener = new SyncConnectivityListener(this, syncButton);
    	
    	syncButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			
    			if(syncConnectivityListener.isConnected() && makeSureNotEditing()){
    				
    				SyncProgressDialog.show(activity);
    				
    				getThreadPool().execute(new Runnable(){
    					@Override
    					public void run(){
    					
    						new ArbiterCookieManager(getApplicationContext()).updateAllCookies();
    						
    						runOnUiThread(new Runnable(){
    	    					
    	    					@Override
    	    					public void run(){
    	    						
    	    						Map.getMap().sync(cordovaWebView);
    	    					}
    	    				});
    					}
    				});
    			}
    		}
    	});
    	
    	cookieConnectivityListener = new CookieConnectivityListener(this, this, this);
    	
    	ImageButton aoiButton = (ImageButton) findViewById(R.id.AOIButton);
    	
    	aoiButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			Map.getMap().zoomToAOI(cordovaWebView);
    		}
    	});
    	
    	final ImageButton locationButton = (ImageButton) findViewById(R.id.locationButton);
    	
    	locationButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			
    			if(locationButton.getAnimation() == null){
    				
    				Animation rotation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_rotate);
        			
    				rotation.setDuration(2500);
    				
    				// 60000 / 2500
        			rotation.setRepeatCount(24);
        			
        			locationButton.startAnimation(rotation);
        			
        			Map.getMap().zoomToCurrentPosition(cordovaWebView);
    			}
    		}
    	});
    	
    	ImageButton zoomInButton = (ImageButton) findViewById(R.id.zoomInButton);
    	
    	zoomInButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			Map.getMap().zoomIn(cordovaWebView);
    		}
    	});
    	
    	ImageButton zoomOutButton = (ImageButton) findViewById(R.id.zoomOutButton);
    	
    	zoomOutButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			Map.getMap().zoomOut(cordovaWebView);
    		}
    	});
    	
    	initIncompleteProjectHelper();
    	
    	incompleteProjectHelper.setSyncButton(syncButton);
    }
    
    // Return true if not editing
    private boolean makeSureNotEditing(){
    	int editMode = mapChangeHelper.getEditMode();
		
		if(editMode == GeometryEditor.Mode.OFF || editMode == GeometryEditor.Mode.SELECT){
			return true;
		}
			
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(R.string.finish_editing_title);
		builder.setMessage(R.string.finish_editing_message);
		builder.setIcon(R.drawable.icon);
		builder.setPositiveButton(android.R.string.ok, null);
		
		builder.create().show();
		
		return false;
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    }
    
    public NotificationBadge getNotificationBadge(){
    	return this.notificationBadge;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        
        Log.w("MapActivity", "MapActivity onCreateOptionsMenu");
        
        if(this.notificationBadge == null){
        	this.notificationBadge = new NotificationBadge(this, menu);
        }
        
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
    	
    	if(frag != null) {
    		frag.show(getSupportFragmentManager(), InsertFeatureDialog.TAG);
    	}
    }
    
    private void startAOIActivity(){
    	Intent aoiIntent = new Intent(this, AOIActivity.class);
		this.startActivity(aoiIntent);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()) {
    		case R.id.action_new_feature:
    			if(makeSureNotEditing()){
    				openInsertFeatureDialog();
    			}
    			
    			return true;
        	
    		case R.id.action_servers:
        		dialogs.showServersDialog();
        		return true;
        		
        	case R.id.action_projects:
        		if(makeSureNotEditing()){
        			Map.getMap().goToProjects(cordovaWebView);
        		}	
        		
        		return true;

			case R.id.action_tilesets:
				dialogs.showTilesetsDialog();
				return true;
        	
        	case R.id.action_aoi:
        		if(makeSureNotEditing()){
        			if(arbiterProject != null) {
    	        		String openProject = arbiterProject.getOpenProject(this);
    	            	if(openProject.equals(this.getResources().getString(R.string.default_project_name))) {
    	            		
    						this.runOnUiThread(new Runnable(){
    							@Override
    							public void run(){
    								Activity context = getActivity();
    			            		AlertDialog.Builder builder = new AlertDialog.Builder(context);
    								builder.setTitle(context.getResources().getString(R.string.error));
    								builder.setIcon(context.getResources().getDrawable(R.drawable.icon));
    								builder.setMessage(context.getResources().getString(R.string.error_aoi_create_project));
    								
    								builder.create().show();
    							}
    						});
    	            	} else {
    	            		startAOIActivity();
    	            	}
    	        		
            		}
        		}
        		
        		return true;
        		
        	case R.id.action_settings:
        		
        		new Settings(this).displaySettingsDialog(false);
        		
        		return true;
        		
        	case R.id.action_about:
        		
        		new About(this).displayAboutDialog();;
        		
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
    
    private void checkNotificationsAreComputed(){
    	
    	final Activity activity = this;
    	
    	SyncProgressDialog.show(this, getResources().getString(R.string.loading),
    			getResources().getString(R.string.please_wait));
    	
    	getThreadPool().execute(new Runnable(){
    		@Override
    		public void run(){
    			
    			SyncTableHelper helper = new SyncTableHelper(getProjectDatabase());
    	    	
    	    	final Sync sync = helper.checkNotificationsAreComputed();
    	    	
    	    	activity.runOnUiThread(new Runnable(){
    	    		@Override
    	    		public void run(){
    	    			
    	    			if(sync != null && !sync.getNotificationsAreSet()){
    	    				
    	    				Map.getMap().getNotifications(cordovaWebView, Integer.toString(sync.getId()));
    	    			}else{
    	    				
    	    				SyncProgressDialog.dismiss(activity);
    	    			}
    	    		}
    	    	});
    		}
    	});
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
    		
    		getThreadPool().execute(new Runnable(){
				@Override
				public void run(){
					OOMWorkaround oom = new OOMWorkaround(getActivity());
    				oom.resetSavedBounds(false);
    				
    				getActivity().runOnUiThread(new Runnable(){
    					@Override
    					public void run(){
    						
				    		// Setting the aoi
				    		if(ArbiterState.getArbiterState().isSettingAOI()){
				    			Log.w(TAG, TAG + ".onResume() setting aoi");
								SyncProgressDialog.show(getActivity());
								
								getThreadPool().execute(new Runnable(){
									
									@Override
									public void run(){
										
										new ArbiterCookieManager(getApplicationContext()).updateAllCookies();
										
										runOnUiThread(new Runnable(){
											@Override
											public void run(){
												
												updateProjectAOI();
											}
										});
									}
								});
				    		}else if(!arbiterProject.isSameProject(getApplicationContext())){
				    				
			    				arbiterProject.makeSameProject();
								
								Map.getMap().resetWebApp(cordovaWebView);
								
								// If the user changed projects, check to 
			        			// see if the project has an aoi or not
			        			incompleteProjectHelper.checkForAOI();
				    		}
				    		
				    		OnReturnToMap.getInstance().executeJobs(getActivity());
    					}
    				});
				}
			});
    	}
    }
    
    private void updateProjectAOI(){
    	final String aoi = ArbiterState.getArbiterState().getNewAOI();
		
    	updateProjectAOI(aoi);
    }
    
    private void updateProjectAOI(String aoi){
    	
        Map.getMap().updateAOI(cordovaWebView, aoi);
        
        // Set the new aoi to null so the we know we're
        // not setting the aoi anymore.
        ArbiterState.getArbiterState().setNewAOI(null);
    }
    
    @Override
    protected void onDestroy(){
    	this.isDestroyed = true;
    	super.onDestroy();
    	if(this.cordovaWebView != null){
    		Log.w("MapActivity", "MapActivity onDestroy");
    		cordovaWebView.handleDestroy();
    	}
    	
    	if(this.failedSyncHelper != null){
			this.failedSyncHelper.dismiss();
		}
    	
    	if(this.notificationBadge != null){
    		this.notificationBadge.onDestroy();
    	}
    	
    	if(this.syncConnectivityListener != null){
    		this.syncConnectivityListener.onDestroy();
    	}
    	
    	if(this.cookieConnectivityListener != null){
    		this.cookieConnectivityListener.onDestroy();
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
        if(!isDestroyed && message.equals("onPageFinished")){
        	if(obj instanceof String){
        		if(((String) obj).equals("about:blank")){
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

	@Override
	public ConnectivityListener getListener() {
		
		return this.syncConnectivityListener;
	}
}

