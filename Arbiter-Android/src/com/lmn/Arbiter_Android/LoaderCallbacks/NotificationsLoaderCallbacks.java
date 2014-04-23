package com.lmn.Arbiter_Android.LoaderCallbacks;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.NotificationsActivity;
import com.lmn.Arbiter_Android.ListAdapters.NotificationsAdapter;
import com.lmn.Arbiter_Android.Loaders.NotificationsLoader;
import com.lmn.Arbiter_Android.Notifications.NotificationListItem;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public class NotificationsLoaderCallbacks implements LoaderManager.LoaderCallbacks<ArrayList<NotificationListItem>>{
	private NotificationsActivity activity;
	private NotificationsAdapter notificationsAdapter;
	
	public NotificationsLoaderCallbacks(NotificationsActivity activity, NotificationsAdapter notificationsAdapter){
		this.activity = activity;
		
		this.notificationsAdapter = notificationsAdapter;
		
		// Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
		activity.getSupportLoaderManager().initLoader(R.id.loader_notifications, null, this);
	}
	
	@Override
	public Loader<ArrayList<NotificationListItem>> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new NotificationsLoader(activity);
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<NotificationListItem>> loader, ArrayList<NotificationListItem> data) {
		notificationsAdapter.setData(data);
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<NotificationListItem>> loader) {
		notificationsAdapter.setData(null);
	}
}
