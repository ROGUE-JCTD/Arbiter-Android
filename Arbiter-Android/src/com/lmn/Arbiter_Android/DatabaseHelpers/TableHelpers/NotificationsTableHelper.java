package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.Notifications.Notification;
import com.lmn.Arbiter_Android.Notifications.NotificationListItem;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.SparseArray;

public class NotificationsTableHelper implements BaseColumns{

	public static final String TABLE_NAME = "notifications";
	public static final String SYNC_ID = "syncId";
	public static final String LAYER_ID = "layerId";
	public static final String FID = "fid";
	public static final String STATE = "state";
	
	private SQLiteDatabase db;
	
	public NotificationsTableHelper(SQLiteDatabase db){
		this.db = db;
	}
	
	public void createTable(){
		
		String sql = "CREATE TABLE " + TABLE_NAME + " (" 
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ SYNC_ID + " INTEGER REFERENCES " + SyncTableHelper.TABLE_NAME + "(" + SyncTableHelper._ID + ") ON DELETE CASCADE, " 
				+ LAYER_ID + " INTEGER REFERENCES " + LayersHelper.LAYERS_TABLE_NAME + "(" + LayersHelper._ID + ") ON DELETE CASCADE, "
				+ FID + " TEXT, "
				+ STATE + " TEXT);";
		
		this.db.execSQL(sql);
	}
	
	public ArrayList<NotificationListItem> getNotifications(SyncTableHelper syncTableHelper){
		
		SparseArray<SparseArray<ArrayList<Notification>>> notificationsMap = buildNotifications();
		
		ArrayList<NotificationListItem> notificationsList = getNotificationsList(notificationsMap, syncTableHelper);
		
		return notificationsList;
	}
	
	private ArrayList<NotificationListItem> getNotificationsList(SparseArray<SparseArray<ArrayList<Notification>>> notificationsMap, SyncTableHelper syncTableHelper){
		
		ArrayList<NotificationListItem> notificationsList = new ArrayList<NotificationListItem>();
		
		int syncKey;
		int layerKey;
		Layer layer = null;
		
		SparseArray<ArrayList<Notification>> syncNotifications;
		
		for(int i = 0, syncCount = notificationsMap.size(); i < syncCount; i++){
			
			syncKey = notificationsMap.keyAt(i);
			
			syncNotifications = notificationsMap.get(syncKey);
			
			// Add the sync to the list
			notificationsList.add(syncTableHelper.getSyncById(syncKey));
			
			for(int j = 0, layerCount = syncNotifications.size(); j < layerCount; j++){
				
				layerKey = syncNotifications.keyAt(j);
				
				layer = LayersHelper.getLayersHelper().get(db, layerKey);
				
				layer.setSyncId(syncKey);
				
				// Add the layer to the list
				notificationsList.add(layer);
				
				// Add all of the notifications for the layer
				notificationsList.addAll(syncNotifications.get(layerKey));
			}
		}
		
		return notificationsList;
	}
	
	private SparseArray<SparseArray<ArrayList<Notification>>> buildNotifications(){
		
		String[] columns = {
			_ID,
			SYNC_ID,
			LAYER_ID,
			FID,
			STATE
		};
		
		String orderBy = SYNC_ID;
		
		Cursor cursor = this.db.query(TABLE_NAME, columns, null, null, null, null, orderBy);
		
		int id;
		int syncId;
		int layerId;
		String fid;
		String state;
		
		// TODO: Make this cleaner.
		// Keep track of layers
		SparseArray<SparseArray<ArrayList<Notification>>> notificationsMap = new SparseArray<SparseArray<ArrayList<Notification>>>();
		
		SparseArray<ArrayList<Notification>> syncNotifications = null;
		ArrayList<Notification> layerNotifications = null;
		
		Notification notification = null;
		
		//Traverse the cursors to populate the projects array
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			
			id = cursor.getInt(0);
			syncId = cursor.getInt(1);
			layerId = cursor.getInt(2);
			fid = cursor.getString(3);
			state = cursor.getString(4);
			
			notification = new Notification(id, syncId,
					layerId, fid, state);
			
			syncNotifications = notificationsMap.get(syncId);
			
			if(syncNotifications == null){
				
				syncNotifications = new SparseArray<ArrayList<Notification>>();
				notificationsMap.put(syncId, syncNotifications);
			}
			
			layerNotifications = syncNotifications.get(layerId);
			
			if(layerNotifications == null){
				
				layerNotifications = new ArrayList<Notification>();
				syncNotifications.put(layerId, layerNotifications);
			}
			
			layerNotifications.add(notification);
		}
		
		cursor.close();
		
		return notificationsMap;
	}
	
	public void deleteById(int id){
		
		this.db.beginTransaction();
		
		try{
			
			String whereClause = _ID + "=?";
			
			String[] whereArgs = {
				Integer.toString(id)
			};
			
			this.db.delete(TABLE_NAME, whereClause, whereArgs);
			
			this.db.setTransactionSuccessful();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			this.db.endTransaction();
		}
	}
	
	public void deleteByLayerId(int syncId, int layerId){
		
		this.db.beginTransaction();
		
		try{
			
			String whereClause = SYNC_ID + "=? AND " + LAYER_ID + "=?";
			
			String[] whereArgs = {
				Integer.toString(syncId),
				Integer.toString(layerId)
			};
			
			this.db.delete(TABLE_NAME, whereClause, whereArgs);
			
			this.db.setTransactionSuccessful();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			this.db.endTransaction();
		}
	}
	
	public int getNotificationsCount(){
		
		String[] columns = {
			"COUNT(*)"
		};
		
		Cursor cursor = this.db.query(TABLE_NAME, columns, null, null, null, null, null);
		
		int count = 0;
		
		if(cursor.moveToFirst()){
			count = cursor.getInt(0);
		}
		
		cursor.close();
		
		return count;
	}
}
