package com.lmn.Arbiter_Android;

import com.lmn.Arbiter_Android.MenuEvents.MapMenuEvents;
import com.lmn.Arbiter_Android.ProjectWizard.ProjectWizard;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

@SuppressLint("SetJavaScriptEnabled")
public class MapActivity extends FragmentActivity {
        private WebView mWebView;
        private MapMenuEvents menuEvents;
        
        public MapActivity(){
        	super();
        	menuEvents = new MapMenuEvents();
        }
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_map);

            mWebView = (WebView) findViewById(R.id.webView1);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.loadUrl("http://www.javacodegeeks.com");
            
            startWizard();
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
        			menuEvents.activateAddFeatures();;
        			return true;
        		
        		case R.id.action_remove_feature:
        			menuEvents.activateRemoveFeatures();
        			return true;
        		
        		case R.id.action_sync:
        			menuEvents.sync();
        			return true;
        	
	        	case R.id.action_layers:
	        		menuEvents.showLayers(this);
	        		return true;
	        	
	        	case R.id.action_projects:
	        		menuEvents.showProjects(this);
	        		return true;
	        		
	        	case R.id.action_modified:
	        		menuEvents.showModified(this);
	        		return true;
	        		
	        	case R.id.action_settings:
	        		menuEvents.showSettings(this);
	        		return true;
        		
        		default:
        			return super.onOptionsItemSelected(item);
        	}
        }
        
        public void startWizard(){
        	(new ProjectWizard(getSupportFragmentManager(), getResources())).startWizard();
        }
}

