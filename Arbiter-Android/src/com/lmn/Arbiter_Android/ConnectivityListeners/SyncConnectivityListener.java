package com.lmn.Arbiter_Android.ConnectivityListeners;

import com.lmn.Arbiter_Android.R;

import android.app.Activity;
import android.widget.ImageButton;

public class SyncConnectivityListener extends ConnectivityListener {
	private ImageButton syncButton;
	private int green;
	private int red;
	
	public SyncConnectivityListener(Activity activity, ImageButton syncButton){
		super(activity);
		
		this.syncButton = syncButton;
		this.green = activity.getApplicationContext().getResources().getColor(R.color.transparent_green);
		this.red = activity.getApplicationContext().getResources().getColor(R.color.transparent_red);
	}
	
	@Override
	public void onConnectivityChanged(boolean isConnected){
		
		if(isConnected){
			onConnected();
		}else{
			onDisconnected();
		}
	}
	
	private void onConnected(){
		syncButton.setBackgroundColor(green);
		syncButton.setEnabled(true);
	}
	
	private void onDisconnected(){
		syncButton.setBackgroundColor(red);
		//syncButton.setEnabled(false);
		syncButton.setEnabled(true);
	}
}
