package com.lmn.Arbiter_Android.Notifications;

import java.text.ParseException;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.SyncTableHelper;
import com.lmn.Arbiter_Android.Loaders.NotificationsLoader;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;
import com.lmn.Arbiter_Android.TimeZone.LocalTime;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Sync extends NotificationListItem{

	private Integer syncId;
	private String timestamp;
	private boolean notificationsAreSet;
	
	public Sync(Integer syncId, String timestamp, String notificationsAreSet){
		this.syncId = syncId;
		this.timestamp = timestamp;
		this.notificationsAreSet = Boolean.parseBoolean(notificationsAreSet);
	}
	
	public Integer getId(){
		return this.syncId;
	}
	
	public String getTimestamp(){
		return this.timestamp;
	}
	
	public boolean getNotificationsAreSet(){
		return this.notificationsAreSet;
	}
	
	@Override
	public void setNotificationView(View view, final Activity activity){
		
		super.setNotificationView(view, activity);
		
		view.setBackgroundColor(activity.getResources().getColor(android.R.color.background_dark));
		
		TextView syncTimeTextView = (TextView) view.findViewById(R.id.notification_sync_time);
		Button deleteBtn = (Button) view.findViewById(R.id.deleteButton);
		
		try {
			syncTimeTextView.setText(getFormattedDate());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		syncTimeTextView.setEnabled(true);
		syncTimeTextView.setVisibility(View.VISIBLE);
		
		deleteBtn.setTextColor(activity.getResources().getColor(android.R.color.white));
		
		deleteBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				delete(activity);
			}
		});
	}
	
	private String getFormattedDate() throws ParseException{
		
		String datetime = (new LocalTime(this.timestamp, true)).getLocalCalendar().getTime().toString();
		
		return datetime;
	}
	
	private void delete(final Activity activity){
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				
				String projectName = ArbiterProject.getArbiterProject().getOpenProject(activity);
				
				String projectPath = ProjectStructure.getProjectPath(projectName);
				
				SQLiteDatabase db = ProjectDatabaseHelper.getHelper(activity.getApplicationContext(), projectPath, false).getWritableDatabase();
				
				SyncTableHelper syncTableHelper = new SyncTableHelper(db);
				
				syncTableHelper.deleteById(syncId);
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						
						LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(
								new Intent(NotificationsLoader.NOTIFICATIONS_UPDATED));
					}
				});
			}
		});
	}
}
