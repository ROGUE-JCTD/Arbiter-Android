package com.lmn.Arbiter_Android.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.AsyncTaskLoader;

public class NotificationsBroadcastReceiver<Type> extends BroadcastReceiver {
	private AsyncTaskLoader<Type> loader;
	
	public NotificationsBroadcastReceiver(AsyncTaskLoader<Type> notificationLoader){
		this.loader = notificationLoader;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		loader.onContentChanged();
	}

}
