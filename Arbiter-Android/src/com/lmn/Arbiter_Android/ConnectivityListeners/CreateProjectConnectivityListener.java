package com.lmn.Arbiter_Android.ConnectivityListeners;

import android.app.AlertDialog;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;

public class CreateProjectConnectivityListener extends ConnectivityListener {
	private FragmentActivity activity;
	private ArbiterDialogs dialogs;
	private MenuItem createProjectBtn;
	private MenuItem.OnMenuItemClickListener onConnectedListener;
	private MenuItem.OnMenuItemClickListener onDisconnectedListener;
	
	public CreateProjectConnectivityListener(FragmentActivity activity, Menu menu){
		super(activity);
		
		this.activity = activity;
		this.createProjectBtn = menu.getItem(0);
		this.dialogs = new ArbiterDialogs(activity.getApplicationContext(),
				activity.getResources(),
				activity.getSupportFragmentManager());
		
		onConnectivityChanged(isConnected());
	}
	
	@Override
	public void onConnectivityChanged(boolean isConnected){
		
		if(isConnected){
			onConnected();
		}else{
			//onDisconnected();
			onConnected();
		}
	}
	
	private MenuItem.OnMenuItemClickListener getOnConnectedListener(){
		
		if(onConnectedListener == null){
			
			final CreateProjectConnectivityListener listener = this;
			
			onConnectedListener = new MenuItem.OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					if(item.getItemId() == R.id.action_new_project){
						dialogs.showProjectNameDialog(listener);
						
						return true;
					}

					return false;
				}
			};
		}
		
		return onConnectedListener;
	}
	
	private MenuItem.OnMenuItemClickListener getOnDisconnectedListener(){
		if(onDisconnectedListener == null){
			onDisconnectedListener = new MenuItem.OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					if(item.getItemId() == R.id.action_new_project){
						
						AlertDialog.Builder builder = new AlertDialog.Builder(activity);
						
						builder.setTitle(R.string.no_network);
						builder.setIcon(activity.getResources().getDrawable(R.drawable.icon));
						builder.setMessage(R.string.check_network_connection);
						
						builder.create().show();
						
						return true;
					}

					return false;
				}
			};
		}
		
		return onDisconnectedListener;
	}
	
	private void onConnected(){
		createProjectBtn.setOnMenuItemClickListener(
				getOnConnectedListener());
	}
	
	private void onDisconnected(){
		createProjectBtn.setOnMenuItemClickListener(
				getOnDisconnectedListener());
	}
}
