package com.lmn.Arbiter_Android.ConnectivityListeners;

import android.content.Context;
import android.widget.ImageButton;

public class AddLayersConnectivityListener extends ConnectivityListener {
	private ImageButton addLayersBtn;
	
	public AddLayersConnectivityListener(Context context, ImageButton addLayersBtn){
		super(context);
		
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
