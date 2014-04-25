package com.lmn.Arbiter_Android.Notifications;

import com.lmn.Arbiter_Android.R;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NotificationListItem {
	
	public NotificationListItem(){
		
	}
	
	public void setNotificationView(View view, Activity activity){
	
		TextView syncTimeTextView = (TextView) view.findViewById(R.id.notification_sync_time);
		TextView layerTitleTextView = (TextView) view.findViewById(R.id.notification_layer_title);
		RelativeLayout featureNotificationLayout = (RelativeLayout) view.findViewById(R.id.notification_feature);
		
		syncTimeTextView.setVisibility(View.GONE);
		layerTitleTextView.setVisibility(View.GONE);
		featureNotificationLayout.setVisibility(View.GONE);
	}
}
