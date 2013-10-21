package com.lmn.Arbiter_Android.LoaderCallbacks;

import com.lmn.Arbiter_Android.ProjectsActivity;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;
import com.lmn.Arbiter_Android.Projects.ProjectListAdapter;
import com.lmn.Arbiter_Android.Projects.ProjectListItem;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public class ProjectsLoaderCallbacks implements LoaderManager.LoaderCallbacks<ProjectListItem[]>{
	private Context context;
	private ProjectListAdapter projectAdapter;
	
	public ProjectsLoaderCallbacks(ProjectsActivity activity, ProjectListAdapter projectAdapter){
		this.context = activity.getApplicationContext();
		
		this.projectAdapter = projectAdapter;
		
		// Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
		activity.getSupportLoaderManager().initLoader(R.id.loader_projects, null, this);
	}
	
	@Override
	public Loader<ProjectListItem[]> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new ProjectsListLoader(context);
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
