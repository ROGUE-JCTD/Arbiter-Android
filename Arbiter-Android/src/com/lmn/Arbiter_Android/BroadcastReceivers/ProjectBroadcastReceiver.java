package com.lmn.Arbiter_Android.BroadcastReceivers;

import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ProjectBroadcastReceiver extends BroadcastReceiver {
	private ProjectsListLoader loader;
	
	public ProjectBroadcastReceiver(ProjectsListLoader projectsListLoader){
		this.loader = projectsListLoader;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		loader.onContentChanged();
	}

}
