package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import com.lmn.Arbiter_Android.Notifications.Sync;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class SyncTableHelper implements BaseColumns{
	
	public static final String TABLE_NAME = "syncs";
	public static final String TIMESTAMP = "timestamp";
	public static final String NOTIFICATIONS_ARE_SET = "notifications_are_set";
	
	private SQLiteDatabase db;
	
	public SyncTableHelper(SQLiteDatabase db){
		this.db = db;
	}
	
	public void createTable(){
		String sql = "CREATE TABLE " + TABLE_NAME + " (" 
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ TIMESTAMP + " TEXT, " 
				+ NOTIFICATIONS_ARE_SET + " BOOLEAN);";
		
		this.db.execSQL(sql);
	}
	
	public Sync getSyncById(int syncId){
		
		String[] columns = {
			TIMESTAMP
		};
		
		String selection = _ID + "=?";
		String[] selectionArgs = {
			Integer.toString(syncId)
		};
		
		Cursor cursor = this.db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
		
		Sync sync = null;
		
		if(cursor.moveToFirst()){
			sync = new Sync(syncId, cursor.getString(0));
		}
		
		return sync;
	}
	
	public void deleteById(int syncId){
		
		this.db.beginTransaction();
		
		try{
			
			String whereClause = _ID + "=?";
			String[] whereArgs = {
				Integer.toString(syncId)
			};
			
			this.db.delete(TABLE_NAME, whereClause, whereArgs);
			
			this.db.setTransactionSuccessful();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			this.db.endTransaction();
		}
	}
}
