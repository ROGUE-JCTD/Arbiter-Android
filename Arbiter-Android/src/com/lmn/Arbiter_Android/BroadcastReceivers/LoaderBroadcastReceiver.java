package com.lmn.Arbiter_Android.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.Loader;

public class LoaderBroadcastReceiver extends BroadcastReceiver {
	private Loader loader;
	
	public LoaderBroadcastReceiver(Loader loader){
		this.loader = loader;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		loader.onContentChanged();
	}

}
