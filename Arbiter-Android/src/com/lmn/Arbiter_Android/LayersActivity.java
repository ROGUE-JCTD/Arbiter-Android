package com.lmn.Arbiter_Android;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ToggleButton;

public class LayersActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_layers);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_layers, menu);
        return true;
    }
	
	public void toggleLayerVisibility(View view){
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();
		
		if (on) {
			
		} else {
			
		}
	}
}
