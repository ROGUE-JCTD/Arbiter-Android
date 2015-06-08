package com.lmn.Arbiter_Android.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lmn.Arbiter_Android.Loaders.AddTilesetsListLoader;

public class AddTilesetsBroadcastReceiver extends BroadcastReceiver {
	private AddTilesetsListLoader loader;

	public AddTilesetsBroadcastReceiver(AddTilesetsListLoader loader){
		this.loader = loader;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		loader.onContentChanged();
	}

}
