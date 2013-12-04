package com.lmn.Arbiter_Android.BroadcastReceivers;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.BaseClasses.Layer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.AsyncTaskLoader;

public class LayerBroadcastReceiver extends BroadcastReceiver {
	private AsyncTaskLoader<ArrayList<Layer>> loader;
	
	public LayerBroadcastReceiver(AsyncTaskLoader<ArrayList<Layer>> layersListLoader){
		this.loader = layersListLoader;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		loader.onContentChanged();
	}

}
