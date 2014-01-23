package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class FailedSync implements BaseColumns{
	public static final String TABLE_NAME = "failed_sync";
	public static final String KEY = "key";
	public static final String DATA_TYPE = "data_type";
	public static final String SYNC_TYPE = "sync_type";
	public static final String LAYER_ID = "layer_id";
	
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
					LAYER_ID + " INTEGER);";
		
		db.execSQL(sql);
	}
}
