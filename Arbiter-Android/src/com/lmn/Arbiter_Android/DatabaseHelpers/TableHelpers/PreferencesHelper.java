package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import android.content.ContentValues;
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
	
	public long insert(SQLiteDatabase db, Context context, String key, String value){
		
		db.beginTransaction();
		long affected = -1;
		
		try {
			ContentValues values = new ContentValues();
			values.put(KEY, key);
			values.put(VALUE, value);
			
			affected = db.insert(TABLE_NAME, null, values);
			
			// If the project successfully inserted,
			// insert the layers with the projects id
			if(affected != -1){
				db.setTransactionSuccessful();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
		return affected;
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
	
	public void update(SQLiteDatabase db, Context context, String key, String value){
		db.beginTransaction();
		
		try {
			String whereClause = KEY + "=?";
			String[] whereArgs = {
				key
			};
			
			ContentValues values = new ContentValues();
			values.put(VALUE, value);
			
			db.update(TABLE_NAME, values, whereClause, whereArgs);
			
			db.setTransactionSuccessful();
		} catch (Exception e) {
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
		String saved = PreferencesHelper.getHelper().get(db,
				context, key);
		
		boolean insert = false;
		
		if(saved == null){
			insert = true;
		}
		
		if(insert){
			this.insert(db, context, key, value);
		}else{
			this.update(db, context, key, value);
		}
	}
}
