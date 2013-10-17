package com.lmn.Arbiter_Android.DatabaseHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lmn.Arbiter_Android.DatabaseHelpers.Schemas.Projects;
import com.lmn.Arbiter_Android.DatabaseHelpers.Schemas.Servers;
import com.lmn.Arbiter_Android.ListItems.ServerListItem;
import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;
import com.lmn.Arbiter_Android.Loaders.ServersListLoader;
import com.lmn.Arbiter_Android.Projects.ProjectListItem;

public class GlobalDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "arbiter_global.db";
	private static final String PROJECTS_TABLE_NAME = "projects";
	private static final String SERVERS_TABLE_NAME = "servers";
	private static int DATABASE_VERSION = 1;
	
	GlobalDatabaseHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		createProjectsTable(db);
		createServersTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO: Migrate the tables
		db.execSQL("DROP TABLE IF EXISTS " + PROJECTS_TABLE_NAME + ";");
		db.execSQL("DROP TABLE IF EXISTS " + SERVERS_TABLE_NAME + ";");
		createProjectsTable(db);
		createServersTable(db);
	}
	
	private void createProjectsTable(SQLiteDatabase db){
		String sql = "CREATE TABLE " + PROJECTS_TABLE_NAME + " (" +
					BaseColumns._ID +
					" INTEGER PRIMARY KEY AUTOINCREMENT, " + 
					Projects.PROJECT_NAME + " TEXT);";
		
		db.execSQL(sql);
	}
	
	private void createServersTable(SQLiteDatabase db){
		String sql = "CREATE TABLE " + SERVERS_TABLE_NAME + " (" +
					BaseColumns._ID +
					" INTEGER PRIMARY KEY AUTOINCREMENT, " +
					Servers.SERVER_NAME + " TEXT, " +
					Servers.SERVER_URL + " TEXT, " +
					Servers.SERVER_USERNAME + " TEXT, " +
					Servers.SERVER_PASSWORD + " TEXT);";
		
		db.execSQL(sql);
	}
	
	// TODO: Strip these helper functions out and separate for each table
	public ServerListItem[] getServers(){
		// Projection - columns to get back
		String[] columns = {Servers.SERVER_NAME, Servers.SERVER_URL, 
				Servers.SERVER_USERNAME, Servers.SERVER_PASSWORD};
		
		// How to sort the results
		String orderBy = Servers.SERVER_NAME + " COLLATE NOCASE";
		
		Cursor cursor =  getWritableDatabase().query(SERVERS_TABLE_NAME, columns, 
				null, null, null, null, orderBy);
		
		ServerListItem[] servers = new ServerListItem[cursor.getCount()];
		
		int i = 0;
		
		//Traverse the cursors to populate the projects array
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			servers[i] = new ServerListItem(cursor.getString(0),
					cursor.getString(1), cursor.getString(2), cursor.getString(3));
			i++;
		}
		
		cursor.close();
		
		return servers;
	}
	
	public void createServer(Context context, ServerListItem server){
		ContentValues values = new ContentValues();
		values.put(Servers.SERVER_NAME, server.getServerName());
		values.put(Servers.SERVER_URL, server.getUrl());
		values.put(Servers.SERVER_USERNAME, server.getUsername());
		values.put(Servers.SERVER_PASSWORD, server.getPassword());
		
		getWritableDatabase().insert(SERVERS_TABLE_NAME, null, values);
		
		LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ServersListLoader.SERVER_LIST_UPDATED));
	}
	
	public ProjectListItem[] getProjects(){
		// Projection - columns to get back
		String[] columns = {Projects.PROJECT_NAME};
		
		// How to sort the results
		String orderBy = Projects.PROJECT_NAME + " COLLATE NOCASE";
		
		Cursor cursor =  getWritableDatabase().query(PROJECTS_TABLE_NAME, columns, 
				null, null, null, null, orderBy);
		
		ProjectListItem[] projects = new ProjectListItem[cursor.getCount()];
		
		String[] columnNames = cursor.getColumnNames();
		
		int i = 0;
		//Traverse the cursors to populate the projects array
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			for(int j = 0; j < columnNames.length; j++){
				if(columnNames[j].equals(Projects.PROJECT_NAME)){
					projects[i] = new ProjectListItem(cursor.getString(j));
					i++;
				}
			}
		}
		
		cursor.close();
		
		return projects;
	}
	
	public void createProject(Context context, String projectName){
		ContentValues values = new ContentValues();
		values.put(Projects.PROJECT_NAME, projectName);
		
		getWritableDatabase().insert(PROJECTS_TABLE_NAME, null, values);
		
		LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ProjectsListLoader.PROJECT_LIST_UPDATED));
	}
}
