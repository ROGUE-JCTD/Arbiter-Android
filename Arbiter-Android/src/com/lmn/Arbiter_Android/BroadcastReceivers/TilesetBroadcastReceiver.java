package com.lmn.Arbiter_Android.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.AsyncTaskLoader;

import com.lmn.Arbiter_Android.BaseClasses.Tileset;

import java.util.ArrayList;

public class TilesetBroadcastReceiver extends BroadcastReceiver {
	private AsyncTaskLoader<ArrayList<Tileset>> loader;

	public TilesetBroadcastReceiver(AsyncTaskLoader<ArrayList<Tileset>> tilesetsListLoader){
		this.loader = tilesetsListLoader;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		loader.onContentChanged();
	}

}
