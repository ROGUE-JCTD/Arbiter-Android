package com.lmn.Arbiter_Android.ConnectivityListeners;

import com.lmn.Arbiter_Android.Activities.HasThreadPool;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.CookieManager.ArbiterCookieManager;
import com.lmn.Arbiter_Android.Map.Map;

import android.app.Activity;
import android.util.SparseArray;

public class CookieConnectivityListener extends ConnectivityListener{

	private Activity activity;
	private HasThreadPool threadPoolContainer;
	private Map.CordovaMap mapContainer;
	
	public CookieConnectivityListener(Activity activity, HasThreadPool threadPoolContainer, Map.CordovaMap mapContainer) {
		super(activity);
		this.activity = activity;
		this.threadPoolContainer = threadPoolContainer;
		this.mapContainer = mapContainer;
	}

	@Override
	public void onConnectivityChanged(boolean isConnected){
		
		if(isConnected){
			
			// Get cookies for each of the servers in case the layers require authentication to be seen.
			this.threadPoolContainer.getThreadPool().execute(new Runnable(){
				
				@Override
				public void run(){
					
					ArbiterCookieManager cookieManager = new ArbiterCookieManager(activity.getApplicationContext());
					
					SparseArray<Server> servers = cookieManager.updateAllCookies();
					
					Server server = null;
					
					for(int i = 0, count = servers.size(); i < count; i++){
						
						server = servers.valueAt(i);
						
						showLayersForServer(Integer.toString(server.getId()));
					}
				}
			});
		}
	}
	
	private void showLayersForServer(final String serverId){
	
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				Map.getMap().showWMSLayersForServer(mapContainer.getWebView(), serverId);
			}
		});
	}
}
