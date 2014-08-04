package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class PreferencesHelper implements BaseColumns{
	public static final String TABLE_NAME = "preferences";
	public static final String KEY = "key";
	public static final String VALUE = "value";
	
	public static final String SAVED_BOUNDS = "saved_bounds";
	public static final String SAVED_ZOOM_LEVEL = "saved_zoom";
	public static final String SHOULD_ZOOM_TO_AOI = "should_zoom_to_aoi";
	public static final String BASE_LAYER = "base_layer";
	public static final String FINDME = "findme";
	public static final String SWITCHED_PROJECT = "switched_project";
	public static final String DOWNLOAD_PHOTOS = "download_photos";
	public static final String DISABLE_WMS = "disable_wms";
	public static final String NO_CON_CHECKS = "no_con_checks";
	public static final String ALWAYS_SHOW_LOCATION = "always_show_location";
	
	private PreferencesHelper(){}
	
	private static PreferencesHelper helper = null;
	
	public static PreferencesHelper getHelper(){
		if(helper == null){
			helper = new PreferencesHelper();
		}
		
		return helper;
	}
	
	public void createTable(SQLiteDatabase db){
		String sql = "CREATE TABLE " + TABLE_NAME + " (" +
					_ID +
					" INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY + " TEXT, " +
					VALUE + " TEXT, " +
					"UNIQUE(" + KEY + "));";
		
		db.execSQL(sql);
	}

	public void delete(SQLiteDatabase db, Context context, String key) {
		db.beginTransaction();
		
		try {
			
			String whereClause = KEY + "=?";
			String[] whereArgs = {
					key	
			};
			
			db.delete(TABLE_NAME, whereClause, whereArgs);
			
			db.setTransactionSuccessful();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
	
	public String get(SQLiteDatabase db, Context context, String key){
		String result = null;
		
		String[] columns = {
				VALUE
		};
		
		String where = KEY + "=?";
		String[] whereArgs = {
			key
		};
		
		Cursor cursor = db.query(TABLE_NAME, columns, where, whereArgs, null, null, null);
		
		boolean hasResult = cursor.moveToFirst();
		
		if(hasResult){
			result = cursor.getString(0);
		}
		
		cursor.close();
		
		return result;
	}
	
	public void put(SQLiteDatabase db, Context context, String key, String value){
		
		db.beginTransaction();
		
		try{
			
			String sql = "INSERT OR REPLACE INTO " + TABLE_NAME 
					+ "(" + KEY + "," + VALUE + ") VALUES (?,?);";
			
			db.execSQL(sql, new Object[]{key, value});
			
			db.setTransactionSuccessful();
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			db.endTransaction();
		}
	}
}
