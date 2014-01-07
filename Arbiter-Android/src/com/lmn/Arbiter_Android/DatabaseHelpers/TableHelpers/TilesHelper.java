package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import android.database.sqlite.SQLiteDatabase;

public class TilesHelper{
	public static final String TABLE_NAME = "tiles";
	public static final String TILESET = "tileset";
	public static final String Z_INDEX = "z";
	public static final String X_INDEX = "x";
	public static final String Y_INDEX = "y";
	public static final String PATH = "path";
	public static final String URL = "url";
	public static final String REF_COUNTER = "ref_counter";
	public static final String ID = "id";
	
	private TilesHelper(){}
	
	private static TilesHelper helper = null;
	
	public static TilesHelper getHelper(){
		if(helper == null){
			helper = new TilesHelper();
		}
		
		return helper;
	}
	
	public void createTable(SQLiteDatabase db){
		String sql = "CREATE TABLE " + TABLE_NAME + " (" +
					ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					TILESET + " TEXT NOT NULL, " +
					Z_INDEX + " INTEGER NOT NULL, " +
					X_INDEX + " INTEGER NOT NULL, " +
					Y_INDEX + " INTEGER NOT NULL, " +
					PATH + " TEXT NOT NULL, " +
					URL + " TEXT NOT NULL, " +
					REF_COUNTER + " INTEGER NOT NULL);";
		
		db.execSQL(sql);
	}
}
