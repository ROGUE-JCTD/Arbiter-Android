package com.lmn.Arbiter_Android.BroadcastReceivers;

import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LoaderBroadcastReceiver extends BroadcastReceiver {
	private ProjectsListLoader loader;
	
	public LoaderBroadcastReceiver(ProjectsListLoader projectsListLoader){
		this.loader = projectsListLoader;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.w("LOADER_BROADCAST_RECEIVER", "ON RECEIVE");
		loader.onContentChanged();
	}

}
