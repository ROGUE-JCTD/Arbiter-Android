package com.lmn.Arbiter_Android;

import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.Projects.ProjectComponents;
import com.lmn.Arbiter_Android.Projects.ProjectListAdapter;
import com.lmn.Arbiter_Android.Projects.ProjectListItem;
import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public class ProjectsActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<ProjectListItem[]>{

	private ListView listView;
	private ArbiterDialogs dialogs;
	private ProjectListAdapter projectAdapter;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_projects);
	    dialogs = new ArbiterDialogs(getResources(), getSupportFragmentManager());
	    
	    this.listView = (ListView) findViewById(R.id.projectListView);
	   // ProjectListAdapter adapter = new ProjectListAdapter(this, R.layout.project_list_item, projectList.getList());
	    this.projectAdapter = new ProjectListAdapter(this.getApplicationContext());
	    this.listView.setAdapter(this.projectAdapter);
	    
	    // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().initLoader(R.id.loader_projects, null, this);
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

	@Override
	public Loader<ProjectListItem[]> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new ProjectsListLoader(this.getApplicationContext());
	}

	@Override
	public void onLoadFinished(Loader<ProjectListItem[]> loader, ProjectListItem[] data) {
		projectAdapter.setData(data);
	}

	@Override
	public void onLoaderReset(Loader<ProjectListItem[]> loader) {
		projectAdapter.setData(null);
	}	
}
