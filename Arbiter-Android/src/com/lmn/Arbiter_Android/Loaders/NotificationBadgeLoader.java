package com.lmn.Arbiter_Android.Loaders;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.BroadcastReceivers.NotificationsBroadcastReceiver;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.NotificationsTableHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class NotificationBadgeLoader extends AsyncTaskLoader<Integer> {
	public static final String NOTIFICATIONS_UPDATED = "NOTIFICATIONS_UPDATED";
	
	private NotificationsBroadcastReceiver<Integer> loaderBroadcastReceiver = null;
	private Integer notifications;
	private Context context;
	private Activity activity;
	
	public NotificationBadgeLoader(Activity activity) {
		super(activity.getApplicationContext());
		
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}

	@Override
	public Integer loadInBackground() {
		
		SQLiteDatabase db = getProjectDatabase();
		
		NotificationsTableHelper notificationHelper = new NotificationsTableHelper(db);
		
		Integer notificationCount = notificationHelper.getNotificationsCount();
		
		return notificationCount;
	}
	
	private SQLiteDatabase getProjectDatabase(){
		
		String projectName = ArbiterProject.getArbiterProject().getOpenProject(activity);
		
		String path = ProjectStructure.getProjectPath(projectName);
		
		return ProjectDatabaseHelper.getHelper(context, path, false).getWritableDatabase();
	}
	
	/**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(Integer _notifications) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (notifications != null) {
          //      onReleaseResources(cursor);
            }
        }
        
        Integer oldNotifications = notifications;
        notifications = _notifications;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(notifications);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldNotifications != null) {
            onReleaseResources(oldNotifications);
        }
    }
    
    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (notifications != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(notifications);
        }

        // Start watching for changes in the app data.
        if (loaderBroadcastReceiver == null) {
        	loaderBroadcastReceiver = new NotificationsBroadcastReceiver<Integer>(this);
        	LocalBroadcastManager.getInstance(getContext()).
        		registerReceiver(loaderBroadcastReceiver, new IntentFilter(NotificationBadgeLoader.NOTIFICATIONS_UPDATED));
        }

        if (takeContentChanged() || notifications == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override public void onCanceled(Integer _notifications) {
        super.onCanceled(_notifications);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(_notifications);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (notifications != null) {
            onReleaseResources(notifications);
            notifications = null;
        }

        // Stop monitoring for changes.
        if (loaderBroadcastReceiver != null) {
        	LocalBroadcastManager.getInstance(getContext()).
        		unregisterReceiver(loaderBroadcastReceiver);
            loaderBroadcastReceiver = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(Integer _notifications) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    	
    }
}
