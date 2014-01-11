package com.lmn.Arbiter_Android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityListener extends BroadcastReceiver{
	private boolean isConnected;
	
	public ConnectivityListener(Context context){
		this.isConnected = false;
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		
		context.registerReceiver(this, intentFilter);
	}
	
	public boolean isConnected(Context context){
		ConnectivityManager cm =
		        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		this.isConnected = isConnected(context);
		
		onConnectivityChanged(this.isConnected);
	}
	
	public void onConnectivityChanged(boolean isConnected){
		
	}
}
