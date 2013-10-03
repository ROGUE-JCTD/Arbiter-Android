package com.lmn.Arbiter_Android;

import com.lmn.Arbiter_Android.MenuEvents.ProjectMenuEvents;
import com.lmn.Arbiter_Android.Projects.ProjectList;
import com.lmn.Arbiter_Android.Projects.ProjectListAdapter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class ProjectsActivity extends FragmentActivity {

	private ListView listView;
	private ProjectList projectList;
	private ProjectMenuEvents menuEvents;
	
	public ProjectsActivity(){
		super();
		
		this.projectList = new ProjectList();
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_projects);
	    
	    this.menuEvents = new ProjectMenuEvents(getSupportFragmentManager(), getResources());
	    
	    this.listView = (ListView) findViewById(R.id.projectListView);
	    ProjectListAdapter adapter = new ProjectListAdapter(this, R.layout.project_list_item, projectList.getList());
	    this.listView.setAdapter(adapter);
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
    			menuEvents.startProjectWizard();
    			return true;
    		
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
}
