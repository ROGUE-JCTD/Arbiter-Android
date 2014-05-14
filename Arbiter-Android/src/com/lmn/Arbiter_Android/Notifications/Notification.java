package com.lmn.Arbiter_Android.Notifications;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.MapActivity;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.NotificationsTableHelper;
import com.lmn.Arbiter_Android.Loaders.NotificationsLoader;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;
import com.lmn.Arbiter_Android.ReturnQueues.OnReturnToMap;
import com.lmn.Arbiter_Android.ReturnQueues.ReturnToActivityJob;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
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
		
		if("MODIFIED".equals(state)){
			
			state = activity.getResources().getString(R.string.modified);
		}else if("ADDED".equals(state)){
			state = activity.getResources().getString(R.string.added);
		}else{
			state = activity.getResources().getString(R.string.removed);
		}
		
		stateTextView.setText(state);
		
		featureNotificationLayout.setVisibility(View.VISIBLE);
		
		view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				zoomToFeatureIfExists(activity);
			}
		});
		
		Button deleteButton = (Button) view.findViewById(R.id.deleteButton);
		
		deleteButton.setTextColor(activity.getResources().getColor(android.R.color.black));
		
		deleteButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				delete(activity);
			}
		});
	}
	
	private void zoomToFeatureIfExists(final Activity activity){
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				
				SQLiteDatabase projectDb = getProjectDb(activity);
				SQLiteDatabase featureDb = getFeatureDb(activity);
				
				final boolean featureExists = featureExists(featureDb, getFeatureType(projectDb));
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						
						if(featureExists){
							
							OnReturnToMap onReturnToMap = OnReturnToMap.getInstance();
							
							onReturnToMap.push(new ReturnToActivityJob(){

								@Override
								public void run(Activity activity) {
									
									try{
										
										Map.getMap().zoomToFeature(((MapActivity)activity).getWebView(), Integer.toString(layerId), fid);
									}catch(ClassCastException e){
										e.printStackTrace();
									}
								}
							});
							
							activity.finish();
						}else{
							
							AlertDialog.Builder builder = new AlertDialog.Builder(activity);
							
							builder.setTitle(activity.getResources().getString(R.string.feature_not_in_aoi));
							builder.setMessage(activity.getResources().getString(R.string.feature_not_in_aoi_msg));
							builder.setPositiveButton(R.string.close, null);
			
							builder.create().show();
						}
					}
				});
			}
		});
	}
	
	private void delete(final Activity activity){
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				
				NotificationsTableHelper notificationHelper = new NotificationsTableHelper(getProjectDb(activity));
				
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
	
	private SQLiteDatabase getProjectDb(Activity activity){
		
		String projectName = ArbiterProject.getArbiterProject().getOpenProject(activity);
		
		String projectPath = ProjectStructure.getProjectPath(projectName);
		
		return ProjectDatabaseHelper.getHelper(activity.getApplicationContext(),
				projectPath, false).getWritableDatabase();
	}
	
	private SQLiteDatabase getFeatureDb(Activity activity){
		
		String projectName = ArbiterProject.getArbiterProject().getOpenProject(activity);
		
		String projectPath = ProjectStructure.getProjectPath(projectName);
		
		return FeatureDatabaseHelper.getHelper(activity.getApplicationContext(),
				projectPath, false).getWritableDatabase();
	}
	
	private String getFeatureType(SQLiteDatabase projectDb){
		
		String featureType = null;
		
		Layer layer = LayersHelper.getLayersHelper().get(projectDb, layerId);
		
		featureType = layer.getFeatureTypeNoPrefix();
		
		return featureType;
	}
	
	private boolean featureExists(SQLiteDatabase featureDb, String featureType){
		
		Feature feature = FeaturesHelper.getHelper().getFeatureByFid(featureDb, fid, featureType);
		
		return (feature == null) ? false : true;
	}
}
