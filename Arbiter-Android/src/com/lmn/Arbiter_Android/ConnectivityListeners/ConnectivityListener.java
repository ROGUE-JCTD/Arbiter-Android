package com.lmn.Arbiter_Android.ConnectivityListeners;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectivityListener extends BroadcastReceiver{
	private boolean isConnected;
	private Activity activity;
	
	public ConnectivityListener(Activity activity){
		//this.isConnected = false;
		this.isConnected = true;
		this.activity = activity;
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		
		activity.getApplicationContext().registerReceiver(this, intentFilter);
	}
	
	private SQLiteDatabase getDb(){
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(this.activity);
		
		String path = ProjectStructure.getProjectPath(projectName);
		
		return ProjectDatabaseHelper.getHelper(activity.getApplicationContext(),
				path, false).getWritableDatabase();
	}
	
	public boolean checkIsConnected(Context context){
		SQLiteDatabase db = getDb();
		
		String noConnectionChecks = PreferencesHelper.getHelper().get(db,
				context, "no_con_checks");
		if (noConnectionChecks != null && noConnectionChecks.equals("true")) {
			return true;
		}
		
		ConnectivityManager cm =
		        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		this.isConnected = checkIsConnected(context);
		onConnectivityChanged(this.isConnected);
	}
	
	public void onConnectivityChanged(boolean isConnected){
		
	}
	
	public boolean isConnected(){
		return this.isConnected;
	}
	
	public void onDestroy(){
		
		activity.getApplicationContext().unregisterReceiver(this);
	}
}
