package com.lmn.Arbiter_Android.ConnectivityListeners;

import android.app.Activity;
import android.widget.ImageButton;

public class AddLayersConnectivityListener extends ConnectivityListener {
	private ImageButton addLayersBtn;
	
	public AddLayersConnectivityListener(Activity activity, ImageButton addLayersBtn){
		super(activity);
		this.addLayersBtn = addLayersBtn;
		
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
		addLayersBtn.setEnabled(true);
	}
	
	private void onDisconnected(){
		addLayersBtn.setEnabled(false);
	}
}
