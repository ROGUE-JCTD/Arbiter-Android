package com.lmn.Arbiter_Android.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.AsyncTaskLoader;

public class ProjectBroadcastReceiver<Type> extends BroadcastReceiver {
	private AsyncTaskLoader<Type> loader;
	
	public ProjectBroadcastReceiver(AsyncTaskLoader<Type> projectsListLoader){
		this.loader = projectsListLoader;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		loader.onContentChanged();
	}

}
