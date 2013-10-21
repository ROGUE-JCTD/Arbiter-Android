package com.lmn.Arbiter_Android;

import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.Projects.ProjectListAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.ProjectsLoaderCallbacks;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class ProjectsActivity extends FragmentActivity{

	private ListView listView;
	private ArbiterDialogs dialogs;
	private ProjectListAdapter projectAdapter;
	@SuppressWarnings("unused")
	private ProjectsLoaderCallbacks projectsLoaderCallbacks;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_projects);
	    dialogs = new ArbiterDialogs(getResources(), getSupportFragmentManager());
	    
	    listView = (ListView) findViewById(R.id.projectListView);
	   // ProjectListAdapter adapter = new ProjectListAdapter(this, R.layout.project_list_item, projectList.getList());
	    this.projectAdapter = new ProjectListAdapter(this.getApplicationContext());
	    this.listView.setAdapter(this.projectAdapter);
	    
	    this.projectsLoaderCallbacks = new ProjectsLoaderCallbacks(this, this.projectAdapter);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_projects, menu);
        return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()) {
    		case R.id.action_new_project:
    			dialogs.showProjectNameDialog();
    			return true;
    		
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
}
