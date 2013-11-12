package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class PreferencesHelper implements BaseColumns{
	public static final String TABLE_NAME = "preferences";
	public static final String KEY = "key";
	public static final String VALUE = "value";
	
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
		
		Log.w("PreferencesHelper", "PreferencesHelper : " + sql);
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
}
