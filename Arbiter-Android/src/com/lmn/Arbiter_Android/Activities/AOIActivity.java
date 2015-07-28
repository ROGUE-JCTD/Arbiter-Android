package com.lmn.Arbiter_Android.Activities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.ArbiterState;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.ConnectivityListeners.ConnectivityListener;
import com.lmn.Arbiter_Android.ConnectivityListeners.HasConnectivityListener;
import com.lmn.Arbiter_Android.CordovaPlugins.ArbiterCordova;
import com.lmn.Arbiter_Android.Map.Map;

public class AOIActivity extends FragmentActivity implements CordovaInterface,
	Map.CordovaMap, HasThreadPool, TileConfirmation, HasConnectivityListener{
	
	private static final String TAG = "AOIActivity";
	private ConnectivityListener connectivityListener;
	
	// For CORDOVA
    private CordovaWebView cordovaWebView;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Config.init(this);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_aoi_dialog);
		
		cordovaWebView = (CordovaWebView) findViewById(R.id.aoiWebView);
		
		connectivityListener = new ConnectivityListener(this);
		
		Init();
	}
	
	private void Init(){
		registerListeners();
	}
	
	private void registerListeners(){
		View cancel = (View) findViewById(R.id.cancelButton);
		final ArbiterProject arbiterProject = ArbiterProject.getArbiterProject();
        final AOIActivity activity = this;
        final boolean isCreatingProject = ArbiterState.getArbiterState().isCreatingProject();
        
        cancel.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		if(isCreatingProject){
        			arbiterProject.doneCreatingProject(
        					activity.getApplicationContext());
        		}
        		
        		activity.finish();
        	}
        });
        
        View ok = (View) findViewById(R.id.okButton);
        
        ok.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		
        		if(connectivityListener != null && connectivityListener.isConnected()){
        			if(isCreatingProject){
            			Map.getMap().getTileCount(cordovaWebView);
            		}else{
            			Map.getMap().setAOI(cordovaWebView);
            		}
        		}else{
        			Util.showNoNetworkDialog(activity);
        		}
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
    	
    	ImageButton zoomInButton = (ImageButton) findViewById(R.id.aoiZoomInButton);
    	
    	zoomInButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			Map.getMap().zoomIn(cordovaWebView);
    		}
    	});
    	
    	ImageButton zoomOutButton = (ImageButton) findViewById(R.id.aoiZoomOutButton);
    	
    	zoomOutButton.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v){
    			Map.getMap().zoomOut(cordovaWebView);
    		}
    	});
	}
	
	@Override
    protected void onPause() {
            super.onPause();
            Log.d(TAG, "onPause");
    }
    
	private void resetSavedBounds(){
		final AOIActivity activity = this;
    	
    	getThreadPool().execute(new Runnable(){
    		@Override
    		public void run(){	
    			activity.runOnUiThread(new Runnable(){
    				@Override
    				public void run(){
    					cordovaWebView.loadUrl(ArbiterCordova.aoiUrl, 5000);
    				}
    			});
    		}
    	});
	}
	
    @Override 
    protected void onResume(){
    	super.onResume();
    	Log.d(TAG, "onResume");
    	
    	resetSavedBounds();
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

    @Override
    public void confirmTileCount(final String count){
    	final AOIActivity activity = this;

    	this.runOnUiThread(new Runnable(){
    		@Override
    		public void run(){

				Map.getMap().setNewProjectsAOI(cordovaWebView);

    			/*AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    			
    			builder.setIcon(R.drawable.icon);
    			builder.setTitle(R.string.warning);
    			
    			String message = activity.getResources()
    					.getString(R.string.tile_cache_warning);
    			
    			message += " " + count;
    			
    			builder.setMessage(message);
    			builder.setNegativeButton(android.R.string.cancel, null);
    			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {

	        			Map.getMap().setNewProjectsAOI(cordovaWebView);
					}
    			});
    			builder.create().show();*/
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
        		if(((String) obj).equals("about:blank")){
        			this.cordovaWebView.loadUrl(ArbiterCordova.aoiUrl);
        		}
        	}
        	
        	this.cordovaWebView.clearHistory();
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

	@Override
	public ConnectivityListener getListener() {
		return connectivityListener;
	}
}
