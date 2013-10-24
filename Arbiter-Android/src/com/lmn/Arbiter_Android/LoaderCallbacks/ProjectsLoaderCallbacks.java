package com.lmn.Arbiter_Android.LoaderCallbacks;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.ProjectsActivity;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.ListAdapters.ProjectListAdapter;
import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public class ProjectsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Project[]>{
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
	public Loader<Project[]> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new ProjectsListLoader(context);
	}

	@Override
	public void onLoadFinished(Loader<Project[]> loader, Project[] data) {
		projectAdapter.setData(data);
	}

	@Override
	public void onLoaderReset(Loader<Project[]> loader) {
		projectAdapter.setData(null);
	}
}
