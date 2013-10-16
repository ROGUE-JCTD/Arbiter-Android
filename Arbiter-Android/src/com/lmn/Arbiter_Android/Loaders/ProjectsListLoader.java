package com.lmn.Arbiter_Android.Loaders;

import java.util.List;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.lmn.Arbiter_Android.Projects.ProjectListItem;

public class ProjectsListLoader extends AsyncTaskLoader<List<ProjectListItem>> {

	public ProjectsListLoader(Context context) {
		super(context);
	}

	@Override
	public List<ProjectListItem> loadInBackground() {
		return null;
	}

}
