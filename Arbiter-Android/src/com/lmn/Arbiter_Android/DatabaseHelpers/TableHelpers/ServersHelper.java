package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;
import com.lmn.Arbiter_Android.Loaders.ServersListLoader;

public class ServersHelper implements BaseColumns{
	public static final String SERVER_NAME = "server_name";
	public static final String SERVER_URL = "url";
	public static final String SERVER_USERNAME = "username";
	public static final String SERVER_PASSWORD = "password";
	public static final String SERVERS_TABLE_NAME = "servers";
	
	private ServersHelper(){}
	
	private static ServersHelper helper = null;
	
	public static ServersHelper getServersHelper(){
		if(helper == null){
			helper = new ServersHelper();
		}
		
		return helper;
	}
	
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
	
	public ArrayList<Server> getAll(SQLiteDatabase db){
		// Projection - columns to get back
		String[] columns = {SERVER_NAME, SERVER_URL, 
				SERVER_USERNAME, SERVER_PASSWORD, _ID};
		
		// How to sort the results
		String orderBy = ServersHelper.SERVER_NAME + " COLLATE NOCASE";
		
		Cursor cursor =  db.query(SERVERS_TABLE_NAME, columns, 
				null, null, null, null, orderBy);
		
		// Create an array list with initial capacity equal to the
		// number of servers +1 for the default OSM server
		ArrayList<Server> servers = new ArrayList<Server>(cursor.getCount() + 1);
		
		//Traverse the cursors to populate the projects array
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			Log.w("SERVERS HELPER", "SERVERS HELPER GET ALL id: " + cursor.getInt(4));
			servers.add(new Server(cursor.getString(0),
					cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4)));
		}
		
		cursor.close();
		
		return servers;
	}
	
	public long[] insert(SQLiteDatabase db, Context context, Server[] newServers){
		
		db.beginTransaction();
		
		long[] serverIds = new long[newServers.length];
		
		try {
			ContentValues values;
			boolean somethingWentWrong = false;
			
			for(int i = 0; i < newServers.length; i++){
				values = new ContentValues();
				values.put(ServersHelper.SERVER_NAME, newServers[i].getServerName());
				values.put(ServersHelper.SERVER_URL, newServers[i].getUrl());
				values.put(ServersHelper.SERVER_USERNAME, newServers[i].getUsername());
				values.put(ServersHelper.SERVER_PASSWORD, newServers[i].getPassword());
					
				serverIds[i] = db.insert(SERVERS_TABLE_NAME, null, values);
				
				if(serverIds[i] == -1){
					somethingWentWrong = true;
					break;
				}
			}
			
			if(!somethingWentWrong){
				db.setTransactionSuccessful();
				
				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServersListLoader.SERVER_LIST_UPDATED));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
		return serverIds;
	}

	public void delete(SQLiteDatabase db, Context context, Server server) {
		Log.w("SERVERSHELPER", "SERVERSHELPER delete");
		db.beginTransaction();
		
		try {
			
			String whereClause = _ID + "=?";
			String[] whereArgs = {
					Long.toString(server.getId())	
			};
			
			int affectedRow = db.delete(SERVERS_TABLE_NAME, whereClause, whereArgs);
			
			Log.w("SERVERSHELPER", "SERVERSHELPER delete" + Integer.toString(affectedRow));
			
			db.setTransactionSuccessful();
			
			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServersListLoader.SERVER_LIST_UPDATED));
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
	
	public void deletionAlert(Activity activity, final Runnable deleteIt){
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(R.string.delete_server_title);
		builder.setIcon(activity.getResources().getDrawable(R.drawable.icon));
		builder.setMessage(R.string.delete_server_alert);
		builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteIt.run();
			}
		});
		
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		
		builder.create().show();
	}
}
