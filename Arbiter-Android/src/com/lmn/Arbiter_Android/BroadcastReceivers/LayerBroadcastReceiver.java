package com.lmn.Arbiter_Android.BroadcastReceivers;

import com.lmn.Arbiter_Android.Loaders.LayersListLoader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LayerBroadcastReceiver extends BroadcastReceiver {
	private LayersListLoader loader;
	
	public LayerBroadcastReceiver(LayersListLoader layersListLoader){
		this.loader = layersListLoader;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		loader.onContentChanged();
	}

}
