package com.lmn.Arbiter_Android.Activities;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.LoaderCallbacks.NotificationBadgeLoaderCallbacks;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NotificationBadge {
	private FragmentActivity activity;
	private MenuItem badgeContainer;
	private Button badge;
	private int notificationCount;
	private NotificationBadgeLoaderCallbacks loader;
	
	public NotificationBadge(FragmentActivity activity, Menu menu){
		
		this.activity = activity;
		
		this.badgeContainer = menu.findItem(R.id.action_notifications);
		
		View view = this.badgeContainer.getActionView();
	        
		view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				startNotificationsActivity();
			}
		});
		
		this.badge = (Button) view.findViewById(R.id.notif_count);
	        
		this.loader = new NotificationBadgeLoaderCallbacks(this.activity, this);
	}
	
	public void setCount(int count){
		this.notificationCount = count;
		
		this.badge.setText(String.valueOf(this.notificationCount));
		
		if(this.notificationCount == 0){
			
			this.badgeContainer.setVisible(false);
		}else{
			this.badgeContainer.setVisible(true);
		}
	}
	
	private void startNotificationsActivity(){
    	Intent notificationsIntent = new Intent(activity, NotificationsActivity.class);
    	activity.startActivity(notificationsIntent);
    }
	
	public void onDestroy(){
		
		this.loader.onDestroy();
	}
}
