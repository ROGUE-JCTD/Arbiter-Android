package com.lmn.Arbiter_Android;

import android.content.Context;
import android.widget.ImageButton;

public class SyncConnectivityListener extends ConnectivityListener {
	private ImageButton syncButton;
	private int green;
	private int red;
	
	public SyncConnectivityListener(Context context, ImageButton syncButton){
		super(context);
		
		this.syncButton = syncButton;
		this.green = context.getResources().getColor(R.color.transparent_green);
		this.red = context.getResources().getColor(R.color.transparent_red);
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
		syncButton.setEnabled(false);
	}
}
