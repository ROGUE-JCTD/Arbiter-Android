package com.lmn.Arbiter_Android.BroadcastReceivers;

import com.lmn.Arbiter_Android.Loaders.BaseLayerLoader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BaseLayerBroadcastReceiver extends BroadcastReceiver {
	private BaseLayerLoader loader;
	
	public BaseLayerBroadcastReceiver(BaseLayerLoader baseLayerLoader){
		this.loader = baseLayerLoader;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		loader.onContentChanged();
	}

}
