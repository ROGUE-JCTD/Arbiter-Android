package com.lmn.Arbiter_Android.DatabaseHelpers.Schemas;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lmn.Arbiter_Android.ListItems.ServerListItem;
import com.lmn.Arbiter_Android.Loaders.ServersListLoader;

public class ServersHelper implements ArbiterDatabaseHelper<ServerListItem, ServerListItem[]>, BaseColumns{
	public static final String SERVER_NAME = "server_name";
	public static final String SERVER_URL = "url";
	public static final String SERVER_USERNAME = "username";
	public static final String SERVER_PASSWORD = "password";
	public static final String SERVERS_TABLE_NAME = "servers";
	
	public void createTable(SQLiteDatabase db){
		String sql = "CREATE TABLE " + SERVERS_TABLE_NAME + " (" +
					_ID +
					" INTEGER PRIMARY KEY AUTOINCREMENT, " +
					SERVER_NAME + " TEXT, " +
					SERVER_URL + " TEXT, " +
					SERVER_USERNAME + " TEXT, " +
					SERVER_PASSWORD + " TEXT);";
		
		db.execSQL(sql);
	}
	
	public ServerListItem[] getAll(SQLiteDatabase db){
		// Projection - columns to get back
		String[] columns = {SERVER_NAME, SERVER_URL, 
				SERVER_USERNAME, SERVER_PASSWORD, _ID};
		
		// How to sort the results
		String orderBy = ServersHelper.SERVER_NAME + " COLLATE NOCASE";
		
		Cursor cursor =  db.query(SERVERS_TABLE_NAME, columns, 
				null, null, null, null, orderBy);
		
		ServerListItem[] servers = new ServerListItem[cursor.getCount()];
		
		int i = 0;
		
		//Traverse the cursors to populate the projects array
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			Log.w("SERVERS HELPER", "SERVERS HELPER GET ALL id: " + cursor.getInt(4));
			servers[i] = new ServerListItem(cursor.getString(0),
					cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
			i++;
		}
		
		cursor.close();
		
		return servers;
	}
	
	public void insert(SQLiteDatabase db, Context context, ServerListItem[] newServers){
		
		db.beginTransaction();
		
		try {
			ContentValues values;
			
			for(int i = 0; i < newServers.length; i++){
				values = new ContentValues();
				values.put(ServersHelper.SERVER_NAME, newServers[i].getServerName());
				values.put(ServersHelper.SERVER_URL, newServers[i].getUrl());
				values.put(ServersHelper.SERVER_USERNAME, newServers[i].getUsername());
				values.put(ServersHelper.SERVER_PASSWORD, newServers[i].getPassword());
					
				db.insert(SERVERS_TABLE_NAME, null, values);
			}
			
			db.setTransactionSuccessful();
			
			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServersListLoader.SERVER_LIST_UPDATED));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void delete(SQLiteDatabase db, Context context, ServerListItem[] list) {
		// TODO Auto-generated method stub
		
	}
}
