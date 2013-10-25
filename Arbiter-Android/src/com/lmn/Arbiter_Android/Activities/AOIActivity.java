package com.lmn.Arbiter_Android.Activities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class AOIActivity extends Activity implements CordovaInterface{
	private static final String TAG = "AOIActivity";
	
	// For CORDOVA
    private CordovaWebView cordovaWebview;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_aoi_dialog);
		Config.init(this);
		
		cordovaWebview = (CordovaWebView) findViewById(R.id.aoiWebView);
        
        
        String url = "file:///android_asset/www/index.html";
        cordovaWebview.loadUrl(url, 5000);
		
        View cancel = (View) findViewById(R.id.cancelButton);
        
        final AOIActivity activity = this;
        
        cancel.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		activity.finish();
        	}
        });
        
        View ok = (View) findViewById(R.id.okButton);
        
        ok.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		CommandExecutor.runProcess(new Runnable(){

    				@Override
    				public void run() {
    					GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(activity.getApplicationContext());
    					ProjectsHelper.getProjectsHelper().insert(helper.
    							getWritableDatabase(), activity.getApplicationContext(),
    							ArbiterProject.getArbiterProject().getNewProject());
    					
    					activity.finish();
    				}
    				
    			});
        	}
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.aoi, menu);
		return true;
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
