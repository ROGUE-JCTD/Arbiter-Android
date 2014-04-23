package com.lmn.Arbiter_Android.Notifications;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.NotificationsTableHelper;
import com.lmn.Arbiter_Android.Loaders.NotificationsLoader;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Notification extends NotificationListItem {

	private String fid;
	private String state;
	private int syncId;
	private int layerId;
	private int id;
	
	public Notification(int id, int syncId, int layerId, String fid, String state){
		this.id = id;
		this.syncId = syncId;
		this.layerId = layerId;
		this.fid = fid;
		this.state = state;
	}
	
	public int getId(){
		return this.id;
	}
	
	public int getSyncId(){
		return this.syncId;
	}
	
	public int getLayerId(){
		return this.layerId;
	}
	
	public String getFID(){
		return this.fid;
	}
	
	public String getState(){
		return this.state;
	}
	
	@Override
	public void setNotificationView(View view, final Activity activity){
		
		super.setNotificationView(view, activity);
		
		view.setBackgroundColor(activity.getResources().getColor(android.R.color.white));
		
		RelativeLayout featureNotificationLayout = (RelativeLayout) view.findViewById(R.id.notification_feature);
		TextView fidTextView = (TextView) view.findViewById(R.id.notification_fid);
		TextView stateTextView = (TextView) view.findViewById(R.id.notification_state);
		
		fidTextView.setText(getFID());
		
		String state = null;
		
		if(!this.state.equals("ADDED") && !this.state.equals("REMOVED")){
			state = "MODIFIED";
		}else{
			state = this.state;
		}
		
		stateTextView.setText(state);
		
		featureNotificationLayout.setVisibility(View.VISIBLE);
		
		Button deleteButton = (Button) view.findViewById(R.id.deleteButton);
		
		deleteButton.setTextColor(activity.getResources().getColor(android.R.color.black));
		
		deleteButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				delete(activity);
			}
		});
	}
	
	private void delete(final Activity activity){
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				
				String projectName = ArbiterProject.getArbiterProject().getOpenProject(activity);
				
				String projectPath = ProjectStructure.getProjectPath(projectName);
				
				SQLiteDatabase db = ProjectDatabaseHelper.getHelper(activity.getApplicationContext(),
						projectPath, false).getWritableDatabase();
				
				NotificationsTableHelper notificationHelper = new NotificationsTableHelper(db);
				
				notificationHelper.deleteById(id);
				
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
