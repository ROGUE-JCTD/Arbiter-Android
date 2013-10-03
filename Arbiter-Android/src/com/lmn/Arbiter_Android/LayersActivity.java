package com.lmn.Arbiter_Android;

import com.lmn.Arbiter_Android.Layers.LayerList;
import com.lmn.Arbiter_Android.Layers.LayerListAdapter;
import com.lmn.Arbiter_Android.MenuEvents.LayerMenuEvents;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

public class LayersActivity extends Activity{

	private ListView listView;
	private LayerList layerList;
	private LayerMenuEvents menuEvents;
	
	public LayersActivity(){
		super();
		
		this.layerList = new LayerList();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_layers);
	    
	    this.menuEvents = new LayerMenuEvents();
	    
	    this.listView = (ListView) findViewById(R.id.layerListView);
	    LayerListAdapter adapter = new LayerListAdapter(this, R.layout.layer_list_item, layerList.getList());
	    this.listView.setAdapter(adapter);
	    //createProgressBar();
	    
	}
	
	public void createProgressBar(){
		ProgressBar progressBar = new ProgressBar(this);
		progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		progressBar.setIndeterminate(true);
		listView.setEmptyView(progressBar);
		
		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
		root.addView(progressBar);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_layers, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()) {
    		case R.id.action_add_layers:
    			menuEvents.showAddLayers(this);
    			return true;
    		
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
	
	public void toggleLayerVisibility(View view){
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();
		
		if (on) {
			
		} else {
			
		}
	}
}
