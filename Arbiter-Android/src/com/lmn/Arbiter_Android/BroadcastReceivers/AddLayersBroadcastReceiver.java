package com.lmn.Arbiter_Android.BroadcastReceivers;

import com.lmn.Arbiter_Android.Loaders.AddLayersListLoader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AddLayersBroadcastReceiver extends BroadcastReceiver {
	private AddLayersListLoader loader;
	
	public AddLayersBroadcastReceiver(AddLayersListLoader loader){
		this.loader = loader;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		loader.onContentChanged();
	}

}
