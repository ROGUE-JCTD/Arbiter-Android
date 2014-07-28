package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.lmn.Arbiter_Android.BaseClasses.FailedSyncObj;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog.MediaSyncHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class FailedSync implements BaseColumns{
	public static final String TABLE_NAME = "failed_sync";
	public static final String KEY = "key";
	public static final String DATA_TYPE = "data_type";
	public static final String SYNC_TYPE = "sync_type";
	public static final String LAYER_ID = "layer_id";
	public static final String ERROR_TYPE = "error_type";
	
	// vector data upload, download and media download
	// media upload is already handled by mediaToSend
	// in the preferences table
	public static class DataType {
		public static final int VECTOR = 0;
		public static final int MEDIA = 1;
	}
	
	public static class SyncType {
		public static final int UPLOAD = 0;
		public static final int DOWNLOAD = 1;
	}
	
	public static class ErrorType {
		
		public static final int UNKNOWN_ERROR = 0;
		public static final int UPDATE_ERROR = 1;
		public static final int UNAUTHORIZED = 2;
		public static final int INTERNAL_SERVER_ERROR = 3;
		public static final int RESOURCE_NOT_FOUND = 4;
		public static final int TIMED_OUT = 5;
		public static final int ARBITER_ERROR = 6;
		public static final int MUST_COMPLETE_UPLOAD_FIRST = 7;
	}
	
	private FailedSync(){}
	
	private static FailedSync helper = null;
	
	public static FailedSync getHelper(){
		if(helper == null){
			helper = new FailedSync();
		}
		
		return helper;
	}
	
	public void createTable(SQLiteDatabase db){
		String sql = "CREATE TABLE " + TABLE_NAME + " (" +
					_ID +
					" INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY + " TEXT, " +
					DATA_TYPE + " INTEGER, " +
					SYNC_TYPE + " INTEGER, " + 
					LAYER_ID + " INTEGER, " + 
					ERROR_TYPE + " INTEGER, " +
					"UNIQUE(" + KEY + "," + DATA_TYPE 
					+ "," + SYNC_TYPE + "," + LAYER_ID + "));";
		
		db.execSQL(sql);
	}
	
	public void remove(SQLiteDatabase db, int layerId){
		
		db.beginTransaction();
		
		try {
			
			String whereClause = LAYER_ID + "=?";
			String[] whereArgs = {
				Long.toString(layerId)
			};
			
			db.delete(TABLE_NAME, whereClause, whereArgs);
			
			db.setTransactionSuccessful();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
	
	public void removeFromMediaToSend(Context context, SQLiteDatabase db, int layerId){
		
		String mediaToSendStr = PreferencesHelper.getHelper().get(db, context, MediaSyncHelper.MEDIA_TO_SEND);
		
		if(mediaToSendStr == null){
			return;
		}
		
		JSONObject mediaToSend = null;
		
		try {
			mediaToSend = new JSONObject(mediaToSendStr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(mediaToSend == null){
			return;
		}
		
		mediaToSend.remove(Integer.toString(layerId));
		
		mediaToSendStr = mediaToSend.toString();
		
		PreferencesHelper.getHelper().put(db, context,
				MediaSyncHelper.MEDIA_TO_SEND, mediaToSendStr);
	}
	
	private String getFeatureType(SQLiteDatabase db, int layerId){
		
		Layer layer = LayersHelper.getLayersHelper().get(db, layerId);
		String featureType = null;
		
		if(layer != null){
			featureType = layer.getFeatureTypeNoPrefix();	
		} else {
			// Layer no longer exists, remove them from the table.
			remove(db, layerId);
		}
		
		
		return featureType;
	}
	
	public FailedSyncObj[] getFailedVectorUploads(SQLiteDatabase db){
		
		String selection = DATA_TYPE + "=? AND " + SYNC_TYPE + "=?";
		
		String[] columns = {
			_ID,
			LAYER_ID,
			KEY,
			ERROR_TYPE
		};
		
		String[] selectionArgs = {
			Integer.toString(DataType.VECTOR),
			Integer.toString(SyncType.UPLOAD)
		};
		
		Cursor cursor = db.query(TABLE_NAME, columns, selection,
				selectionArgs, null, null, null);
		
		List<FailedSyncObj> results = new LinkedList<FailedSyncObj>();
		
		int count = cursor.getCount();
		
		if(count > 0){
			
			int id = -1;
			int layerId = -1;
			String key = null;
			int errorType = -1;
			
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				
				id = cursor.getInt(0);
				layerId = cursor.getInt(1);
				key = cursor.getString(2);
				errorType = cursor.getInt(3);
				
				String featureType = getFeatureType(db, layerId);
				if(featureType != null) {
					
					FailedSyncObj failedSyncObj = new FailedSyncObj(id, key, FailedSync.DataType.VECTOR,
							FailedSync.SyncType.UPLOAD, layerId, errorType, featureType);
					
					results.add(failedSyncObj);
				}
			}
		}
		
		cursor.close();
		
		return results.size() > 0 ? results.toArray(new FailedSyncObj[results.size()]) : null;
	}
	
	public FailedSyncObj[] getFailedVectorDownloads(SQLiteDatabase db){
		
		String selection = DATA_TYPE + "=? AND " + SYNC_TYPE + "=?";
		
		String[] columns = {
			_ID,
			LAYER_ID,
			KEY,
			ERROR_TYPE
		};
		
		String[] selectionArgs = {
			Integer.toString(DataType.VECTOR),
			Integer.toString(SyncType.DOWNLOAD)
		};
		
		Cursor cursor = db.query(TABLE_NAME, columns, selection,
				selectionArgs, null, null, null);
		
		List<FailedSyncObj> results = new LinkedList<FailedSyncObj>();
		
		int count = cursor.getCount();
		
		if(count > 0){
			int id = -1;
			int layerId = -1;
			String key = null;
			int errorType = -1;
			
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				
				id = cursor.getInt(0);
				layerId = cursor.getInt(1);
				key = cursor.getString(2);
				errorType = cursor.getInt(3);
				
				String featureType = getFeatureType(db, layerId);
				if(featureType != null) {
					
					FailedSyncObj failedSyncObj = new FailedSyncObj(id, key, FailedSync.DataType.VECTOR,
							FailedSync.SyncType.DOWNLOAD, layerId, errorType, featureType);
					
					results.add(failedSyncObj);
				}
			}
		}
		
		cursor.close();
		
		return results.size() > 0 ? results.toArray(new FailedSyncObj[results.size()]) : null;
	}

	public FailedSyncObj[] getFailedMediaDownloads(SQLiteDatabase db){
	
		String selection = DATA_TYPE + "=? AND " + SYNC_TYPE + "=?";
		
		String[] columns = {
			_ID,
			LAYER_ID,
			KEY,
			ERROR_TYPE
		};
		
		String[] selectionArgs = {
			Integer.toString(DataType.MEDIA),
			Integer.toString(SyncType.DOWNLOAD)
		};
		
		Cursor cursor = db.query(TABLE_NAME, columns, selection,
				selectionArgs, null, null, null);
		
		FailedSyncObj[] results = null;
		
		int count = cursor.getCount();
		
		if(count > 0){
			results = new FailedSyncObj[cursor.getCount()];
			
			int i = 0;
			
			int id = -1;
			int layerId = -1;
			String key = null;
			int errorType = -1;
			
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				id = cursor.getInt(0);
				layerId = cursor.getInt(1);
				key = cursor.getString(2);
				errorType = cursor.getInt(3);
				
				String featureType = getFeatureType(db, layerId);
				
				if(featureType != null){
					results[i++] = new FailedSyncObj(id, key, FailedSync.DataType.MEDIA, FailedSync.SyncType.DOWNLOAD, layerId, errorType, featureType);
				}
			}
		}
		
		cursor.close();
		
		return results;
	}
}
