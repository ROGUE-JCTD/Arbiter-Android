package com.lmn.Arbiter_Android.BaseClasses;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.NotificationsTableHelper;
import com.lmn.Arbiter_Android.Loaders.NotificationsLoader;
import com.lmn.Arbiter_Android.Notifications.NotificationListItem;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class Layer extends NotificationListItem{
	
	/**
	 * Create a key for the methods requiring a key (removeDuplicates, setCheckedLayers..)
	 * @param layer Layer to create the key with
	 * @return
	 */
	public static String buildLayerKey(Layer layer){
		return Integer.valueOf(layer.getServerId()).toString() + ":" +
				layer.getFeatureType();
	}
	
	private int layerId;
	private String featureType;
	private String srs;
	private String workspace;
	
	// Only for the adapter, for displaying
    private String serverName;
    private String serverUrl;
	private String serverOrigin;
	
	private String title;
	private String boundingBox;
	private String color = null;
	
	private int serverId;
	private int layerOrder;
	
	private int syncId;
	
	// Recycled for whether the layer is checked in the AddLayers List
	// and for the layers visibility
	private boolean checked;
	
	private boolean readOnly;
	
	
	public Layer(int layerId, String featureType, String workspace, int serverId, String serverName, String serverUrl,
			String title, String boundingBox, String color, int layerOrder, boolean checked, String readOnly){
		this.layerId = layerId;
		this.featureType = featureType;
		this.serverName = serverName;
		this.title = title;
		this.boundingBox = boundingBox;
		this.color = color;
		this.srs = null;
		this.serverId = serverId;
		this.serverUrl = serverUrl;
		this.workspace = workspace;
		this.layerOrder = layerOrder;
		this.syncId = -1;
		this.readOnly = Boolean.parseBoolean(readOnly);
		
		setChecked(checked);
	}
	
	public Layer(int layerId, String featureType, String workspace, int serverId, String serverName, String serverUrl,
			String title, String srs, String boundingBox, String color, int layerOrder, boolean checked, String readOnly){
		this(layerId, featureType, workspace, serverId, serverName, serverUrl,
				title, boundingBox, color, layerOrder, checked, readOnly);
		
		this.srs = srs;
	}
	
	// For cloning
	public Layer(Layer item){
		this.layerId = item.getLayerId();
		this.featureType = item.getFeatureType();
		this.serverName = item.getServerName();
		this.title = item.getLayerTitle();
		this.boundingBox = item.getLayerBBOX();
		this.color = item.getColor();
		this.checked = item.isChecked();
		this.srs = item.getSRS();
		this.serverId = item.getServerId();
		this.serverUrl = item.getServerUrl();
		this.workspace = item.getWorkspace();
		this.layerOrder = item.getLayerOrder();
		this.syncId = item.getSyncId();
		this.readOnly = item.isReadOnly();
		this.serverOrigin = item.getServerOrigin();
	}
	
	public Layer(BaseLayer baseLayer){
		this.featureType = baseLayer.getFeatureType();
		this.title = baseLayer.getName();
		this.serverName = baseLayer.getServerName();
		this.serverUrl = baseLayer.getUrl();
		this.serverOrigin = baseLayer.getServerId();
	}
	
	public int getSyncId(){
		return syncId;
	}
	
	public void setSyncId(int syncId){
		this.syncId = syncId;
	}
	
	public int getLayerId(){
		return layerId;
	}
	
	public String getFeatureType(){
		return featureType;
	}
	
	public String getFeatureTypeNoPrefix(){
		if(featureType.contains(":")){
			return featureType.split(":")[1];
		}
		
		return featureType; 
	}
	
	public String getServerName(){
        return serverName;
	}
	
	public void setServerName(String name){
		this.serverName = name;
	}
	
	public String getLayerTitle(){
		return title;
	}

	public String getSRS(){
		return srs;
	}
	
	public String getLayerBBOX(){
		return boundingBox;
	}
	
	public String getColor(){
		return color;
	}
	
	public boolean isChecked(){
		return this.checked;
	}
	
	public boolean isReadOnly(){
		return this.readOnly;
	}
	
	public void setReadOnly(boolean readOnly){
		this.readOnly = readOnly;
	}
	
	public int getServerId(){
		return this.serverId;
	}

	public String getServerOrigin() { return this.serverOrigin; }
	public void setServerOrigin(String origin) { this.serverOrigin = origin; }
	
	public String getServerUrl(){
        return this.serverUrl;
	}
	
	public void setServerUrl(String url){
		this.serverUrl = url;
	}
	
	public void setChecked(boolean check){
		this.checked = check;
	}
	
	public void setColor(String color){
		this.color = color;
	}
	
	public String getWorkspace(){
		return this.workspace;
	}
	
	public int getLayerOrder(){
		return this.layerOrder;
	}
	
	public void setLayerOrder(int layerOrder){
		this.layerOrder = layerOrder;
	}
	
	@Override
	public void setNotificationView(View view, final Activity activity){
		
		super.setNotificationView(view, activity);
		
		view.setBackgroundColor(Color.parseColor(ColorMap.COLOR_MAP.get(getColor())));
		
		TextView layerTitleTextView = (TextView) view.findViewById(R.id.notification_layer_title);
		
		Button deleteButton = (Button) view.findViewById(R.id.deleteButton);
		
		layerTitleTextView.setText(getLayerTitle());
		
		layerTitleTextView.setVisibility(View.VISIBLE);
		
		deleteButton.setTextColor(activity.getResources().getColor(android.R.color.white));
		
		deleteButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				deleteNotifications(activity);
			}
		});
	}
	
	public void deleteNotifications(final Activity activity){
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				
				String projectName = ArbiterProject.getArbiterProject().getOpenProject(activity);
				
				String projectPath = ProjectStructure.getProjectPath(projectName);
				
				SQLiteDatabase db = ProjectDatabaseHelper.getHelper(activity.getApplicationContext(), projectPath, false).getWritableDatabase();
				
				NotificationsTableHelper notificationsTableHelper = new NotificationsTableHelper(db);
				
				Log.w("Layer", "Layer syncID = " + syncId);
				
				notificationsTableHelper.deleteByLayerId(syncId, layerId);
				
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
