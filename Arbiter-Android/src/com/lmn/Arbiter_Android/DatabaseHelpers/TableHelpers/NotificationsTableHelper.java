package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

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
				+ SYNC_ID + " INTEGER, " 
				+ LAYER_ID + " INTEGER, "
				+ FID + " TEXT, "
				+ STATE + " TEXT);";
		
		this.db.execSQL(sql);
	}
}
