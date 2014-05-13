package com.lmn.Arbiter_Android.Activities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ConnectivityListeners.CreateProjectConnectivityListener;
import com.lmn.Arbiter_Android.ListAdapters.NotificationsAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.NotificationsLoaderCallbacks;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ListView;

public class NotificationsActivity extends FragmentActivity implements HasThreadPool{

	private ListView listView;
	private NotificationsAdapter notificationsAdapter;
	@SuppressWarnings("unused")
	private NotificationsLoaderCallbacks notificationsLoaderCallbacks;
	
	@SuppressWarnings("unused")
	private CreateProjectConnectivityListener connectivityListener;
	
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_notifications);
	    
	    this.listView = (ListView) findViewById(R.id.notificationsListView);
	    
	    this.notificationsAdapter = new NotificationsAdapter(this);
	    this.listView.setAdapter(this.notificationsAdapter);
	    this.notificationsLoaderCallbacks = new NotificationsLoaderCallbacks(this, this.notificationsAdapter);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		getSupportLoaderManager().destroyLoader(R.id.loader_notifications);
	}

	@Override
	public ExecutorService getThreadPool() {
		
		return threadPool;
	}
}
