package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import android.database.sqlite.SQLiteDatabase;

public class TileIdsHelper{
	public static final String TABLE_NAME = "tileIds";
	public static final String ID = "id";
	
	private TileIdsHelper(){}
	
	private static TileIdsHelper helper = null;
	
	public static TileIdsHelper getHelper(){
		if(helper == null){
			helper = new TileIdsHelper();
		}
		
		return helper;
	}
	
	public void createTable(SQLiteDatabase db){
		String sql = "CREATE TABLE " + TABLE_NAME + " (" +
					ID + " INTEGER PRIMARY KEY AUTOINCREMENT);";
		
		db.execSQL(sql);
	}
}
