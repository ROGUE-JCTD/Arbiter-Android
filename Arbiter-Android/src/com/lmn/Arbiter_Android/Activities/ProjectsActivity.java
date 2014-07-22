package com.lmn.Arbiter_Android.Activities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ConnectivityListeners.CreateProjectConnectivityListener;
import com.lmn.Arbiter_Android.ListAdapters.ProjectListAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.ProjectsLoaderCallbacks;
import com.lmn.Arbiter_Android.ReturnQueues.OnReturnToProjects;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.widget.ListView;

public class ProjectsActivity extends FragmentActivity implements HasThreadPool{

	private ListView listView;
	private ProjectListAdapter projectAdapter;
	@SuppressWarnings("unused")
	private ProjectsLoaderCallbacks projectsLoaderCallbacks;
	private boolean menuPrepared;
	
	@SuppressWarnings("unused")
	private CreateProjectConnectivityListener connectivityListener;
	
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_projects);
	    
	    listView = (ListView) findViewById(R.id.projectListView);
	    
	    this.projectAdapter = new ProjectListAdapter(this);
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
    public boolean onPrepareOptionsMenu(Menu menu){
    	if(!this.menuPrepared){
    		
    		this.connectivityListener = new CreateProjectConnectivityListener(this, menu);
    		
    		this.menuPrepared = true;
    	}
    	
    	return true;
    }
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		getSupportLoaderManager().destroyLoader(R.id.loader_projects);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		OnReturnToProjects.getInstance().executeJobs(this);
	}

	@Override
	public ExecutorService getThreadPool() {
		
		return threadPool;
	}
}
