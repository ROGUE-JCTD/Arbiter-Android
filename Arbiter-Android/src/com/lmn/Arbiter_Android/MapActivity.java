package com.lmn.Arbiter_Android;

import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ToggleButton;

@SuppressLint("SetJavaScriptEnabled")
public class MapActivity extends FragmentActivity {
        private WebView mWebView;
       // private MapMenuEvents menuEvents;
        private ArbiterDialogs dialogs;
        private boolean welcomed;
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_map);
            
            restoreState(savedInstanceState);
            setListeners();
            
            dialogs = new ArbiterDialogs(getResources(), getSupportFragmentManager());

            mWebView = (WebView) findViewById(R.id.webView1);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.setWebViewClient(new WebViewClient(){
            	@Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) 
                {
                    view.loadUrl(url);
                    return true;
                }
            });
            
            mWebView.loadUrl("http://openstreetmap.org");
            
            if(!welcomed){
            	displayWelcomeDialog();
            	welcomed = true;
            }
        }

        /**
         * Set listeners
         */
        public void setListeners(){
        	ImageButton imgButton = (ImageButton) findViewById(R.id.layers_button);
        	imgButton.setOnClickListener(new OnClickListener(){
        		@Override
        		public void onClick(View v){
        			dialogs.showLayersDialog();
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
}

