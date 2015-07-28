package com.lmn.Arbiter_Android.ConnectivityListeners;

import android.app.Activity;
import android.widget.ImageButton;

public class AddTilesetsConnectivityListener extends ConnectivityListener {
	private ImageButton addTilesetsBtn;

	public AddTilesetsConnectivityListener(Activity activity, ImageButton addLayersBtn){
		super(activity);
		this.addTilesetsBtn = addLayersBtn;
		
		onConnectivityChanged(isConnected());
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
		addTilesetsBtn.setEnabled(true);
	}
	
	private void onDisconnected(){
		addTilesetsBtn.setEnabled(false);
	}
}
