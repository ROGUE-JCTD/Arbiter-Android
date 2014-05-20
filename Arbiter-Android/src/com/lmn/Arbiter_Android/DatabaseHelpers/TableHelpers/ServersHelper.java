package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.ListAdapters.ServerTypesAdapter;
import com.lmn.Arbiter_Android.Loaders.ServersListLoader;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class ServersHelper implements BaseColumns{
	public static final String SERVER_TYPE = "type";
	public static final String SERVER_NAME = "server_name";
	public static final String SERVER_URL = "url";
	public static final String SERVER_USERNAME = "username";
	public static final String SERVER_PASSWORD = "password";
	public static final String SERVERS_TABLE_NAME = "servers";
	public static final String GMT_OFFSET = "gmt_offset";

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
					SERVER_TYPE + " TEXT DEFAULT '" + ServerTypesAdapter.Types.WMS + "'," +
					SERVER_NAME + " TEXT, " +
					SERVER_URL + " TEXT, " +
					SERVER_USERNAME + " TEXT, " +
					SERVER_PASSWORD + " TEXT, " + 
					GMT_OFFSET + " INTEGER DEFAULT 0);";
		
		db.execSQL(sql);
	}
	
	public SparseArray<Server> getAll(SQLiteDatabase db){
		// Projection - columns to get back
		String[] columns = {SERVER_TYPE, SERVER_NAME, SERVER_URL, 
				SERVER_USERNAME, SERVER_PASSWORD, _ID};
		
		// How to sort the results
		String orderBy = ServersHelper.SERVER_NAME + " COLLATE NOCASE";
		
		Cursor cursor =  db.query(SERVERS_TABLE_NAME, columns, 
				null, null, null, null, orderBy);
		
		// Create an array list with initial capacity equal to the
		// number of servers +1 for the default OSM server
		SparseArray<Server> servers = new SparseArray<Server>(cursor.getCount() + 1);
		
		int key;
		
		//Traverse the cursors to populate the projects array
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			key = cursor.getInt(5);
			Log.w("ServersHelper", "ServersHelper type = " + cursor.getString(0));
			servers.put(key, new Server(cursor.getString(0),
					cursor.getString(1), cursor.getString(2),
					cursor.getString(3), cursor.getString(4), key));
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
				values.put(SERVER_TYPE, newServers[i].getType());
				values.put(SERVER_NAME, newServers[i].getName());
				values.put(SERVER_URL, newServers[i].getUrl());
				values.put(SERVER_USERNAME, newServers[i].getUsername());
				values.put(SERVER_PASSWORD, newServers[i].getPassword());
					
				serverIds[i] = db.insert(SERVERS_TABLE_NAME, null, values);
				
				if(serverIds[i] == -1){
					somethingWentWrong = true;
					break;
				}
			}
			
			if(!somethingWentWrong){
				db.setTransactionSuccessful();
				
				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServersListLoader.SERVER_LIST_UPDATED));
				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServersListLoader.SERVER_ADDED));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
		return serverIds;
	}

	public void update(SQLiteDatabase db, Context context, Server server){
		db.beginTransaction();
		
		try {
			
			String whereClause = _ID + "=?";
			String[] whereArgs = {
					Long.toString(server.getId())	
			};
			
			ContentValues values = new ContentValues();
			values.put(SERVER_TYPE, server.getType());
			values.put(SERVER_NAME, server.getName());
			values.put(SERVER_USERNAME, server.getUsername());
			values.put(SERVER_PASSWORD, server.getPassword());
			values.put(SERVER_URL, server.getUrl());
			
			db.update(SERVERS_TABLE_NAME, values, whereClause, whereArgs);
			
			db.setTransactionSuccessful();
			
			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServersListLoader.SERVER_LIST_UPDATED));
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
	
	public void delete(Activity activity, Server server) {
		Context context = activity.getApplicationContext();
		
		SQLiteDatabase appDb = ApplicationDatabaseHelper.
				getHelper(context).getWritableDatabase();
		
		appDb.beginTransaction();
		
		try {
			
			String whereClause = _ID + "=?";
			String[] whereArgs = {
					Long.toString(server.getId())	
			};
			
			deleteLayersFromProjects(context, server);
			
			// Delete the server, now that all layers have been deleted
			appDb.delete(SERVERS_TABLE_NAME, whereClause, whereArgs);
			
			appDb.setTransactionSuccessful();
			
			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServersListLoader.SERVER_LIST_UPDATED));
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			appDb.endTransaction();
			
			openOriginalDatabases(activity);
		}
	}
	
	// Delete any layers, in any project, that depend on the server.
	private void deleteLayersFromProjects(Context context, Server server){
		String path = null;
		SQLiteDatabase featureDb = null;
		SQLiteDatabase projectDb = null;
		
		// Get a list of the projects
		Project[] projects = ProjectStructure.
				getProjectStructure().getProjects();

		// Loop through list of projects,
		// cleaning up the servers dependents
		for(int i = 0; i < projects.length;i++){

			// Get the projectDatabase and featureDatabase
			path = ProjectStructure.getProjectPath(projects[i].getProjectName());
			projectDb = ProjectDatabaseHelper.getHelper(context,
					path, false).getWritableDatabase();
			
			featureDb = FeatureDatabaseHelper.getHelper(context,
					path, false).getWritableDatabase();

			// Delete the layers with the serverId
			LayersHelper.getLayersHelper().deleteByServerId(projectDb,
					featureDb, context, server.getId());
		}
	}
	
	private void openOriginalDatabases(Activity activity){
		Context context = activity.getApplicationContext();
		
		String openProjectName = ArbiterProject.
				getArbiterProject().getOpenProject(activity);
		
		String path = ProjectStructure.getProjectPath(openProjectName);
		
		ProjectDatabaseHelper.getHelper(context, path, false);
		FeatureDatabaseHelper.getHelper(context, path, false);
	}
	
	public void deletionAlert(Activity activity, final Runnable deleteIt){
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(R.string.warning);
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
	
	public void updateAlert(final Activity activity, final Runnable updateIt){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				
				builder.setTitle(R.string.warning);
				builder.setIcon(activity.getResources().getDrawable(R.drawable.icon));
				builder.setMessage(R.string.update_server_alert);
				builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						updateIt.run();
					}
				});
				
				builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
				
				builder.create().show();
			}
		});
	}
}
