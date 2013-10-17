package com.lmn.Arbiter_Android.BroadcastReceivers;

import com.lmn.Arbiter_Android.Loaders.ServersListLoader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServerBroadcastReceiver extends BroadcastReceiver {
	private ServersListLoader loader;
	
	public ServerBroadcastReceiver(ServersListLoader serversListLoader){
		this.loader = serversListLoader;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		loader.onContentChanged();
	}

}
