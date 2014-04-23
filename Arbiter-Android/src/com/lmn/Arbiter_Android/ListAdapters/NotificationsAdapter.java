package com.lmn.Arbiter_Android.ListAdapters;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.NotificationsActivity;
import com.lmn.Arbiter_Android.Notifications.NotificationListItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class NotificationsAdapter extends BaseAdapter implements ArbiterAdapter<ArrayList<NotificationListItem>>{
	private Context context;
	private ArrayList<NotificationListItem> notifications;
	private final LayoutInflater inflater;
	private int size;
	private NotificationsActivity activity;
	
	public NotificationsAdapter(NotificationsActivity activity){
			this.context = activity.getApplicationContext();
			this.inflater = LayoutInflater.from(this.context);
			this.notifications = null;
			this.size = 0;
			this.activity = activity;
	}
	
	public void setData(ArrayList<NotificationListItem> notifications){
		
		this.notifications = notifications;
		
		this.size = (notifications != null) ? notifications.size() : 0;
		
		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		// Inflate the layout
		if(view == null){
			view = inflater.inflate(R.layout.notification_list_item, null);
		} 
		
		NotificationListItem item = this.getItem(position);
		
		item.setNotificationView(view, activity);
		
		return view;
	}

	@Override
	public int getCount() {
		return this.size;
	}

	@Override
	public NotificationListItem getItem(int position) {
		return this.notifications.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
