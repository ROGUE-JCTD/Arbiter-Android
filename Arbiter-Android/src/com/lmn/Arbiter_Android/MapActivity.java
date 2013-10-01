package com.lmn.Arbiter_Android;

import com.lmn.Arbiter_Android.ProjectWizard.ProjectWizard;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Menu;
import android.webkit.WebView;

@SuppressLint("SetJavaScriptEnabled")
public class MapActivity extends Activity {
        private WebView mWebView;
        private ProjectWizard projectWizard;
        
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
            getMenuInflater().inflate(R.menu.map, menu);
            return true;
        }
        
        public void startWizard(){
        	projectWizard = new ProjectWizard(getFragmentManager(), getResources());
            projectWizard.startWizard();
        }
}

