package com.lmn.Arbiter_Android.LoaderCallbacks;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.NotificationBadge;
import com.lmn.Arbiter_Android.Loaders.NotificationBadgeLoader;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public class NotificationBadgeLoaderCallbacks implements LoaderManager.LoaderCallbacks<Integer>{
	private FragmentActivity activity;
	private NotificationBadge notificationBadge;
	
	public NotificationBadgeLoaderCallbacks(FragmentActivity activity, NotificationBadge notificationBadge){
		this.activity = activity;
		
		this.notificationBadge = notificationBadge;
		
		// Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
		activity.getSupportLoaderManager().initLoader(R.id.loader_notification_badge, null, this);
	}
	
	@Override
	public Loader<Integer> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new NotificationBadgeLoader(activity);
	}

	@Override
	public void onLoadFinished(Loader<Integer> loader, Integer count) {
		notificationBadge.setCount(count);
	}

	@Override
	public void onLoaderReset(Loader<Integer> loader) {
		notificationBadge.setCount(0);
	}
	
	public void onDestroy(){
		activity.getSupportLoaderManager().destroyLoader(R.id.loader_notification_badge);
	}
}
