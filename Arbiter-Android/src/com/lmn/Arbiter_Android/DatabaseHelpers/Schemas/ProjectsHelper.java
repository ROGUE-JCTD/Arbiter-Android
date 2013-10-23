package com.lmn.Arbiter_Android.DatabaseHelpers.Schemas;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;

import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;
import com.lmn.Arbiter_Android.Projects.ProjectListItem;

public class ProjectsHelper implements ArbiterDatabaseHelper<ProjectListItem, ProjectListItem[]>, BaseColumns{
	public static final String PROJECT_NAME = "name";
	public static final String PROJECTS_TABLE_NAME = "projects";
	
	public void createTable(SQLiteDatabase db){
		String sql = "CREATE TABLE " + PROJECTS_TABLE_NAME + " (" +
					_ID +
					" INTEGER PRIMARY KEY AUTOINCREMENT, " + 
					ProjectsHelper.PROJECT_NAME + " TEXT);";
		
		db.execSQL(sql);
	}
	
	public ProjectListItem[] getAll(SQLiteDatabase db){
		// Projection - columns to get back
		String[] columns = {ProjectsHelper.PROJECT_NAME};
		
		// How to sort the results
		String orderBy = ProjectsHelper.PROJECT_NAME + " COLLATE NOCASE";
		
		Cursor cursor =  db.query(PROJECTS_TABLE_NAME, columns, 
				null, null, null, null, orderBy);
		
		ProjectListItem[] projects = new ProjectListItem[cursor.getCount()];
		
		String[] columnNames = cursor.getColumnNames();
		
		int i = 0;
		//Traverse the cursors to populate the projects array
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			for(int j = 0; j < columnNames.length; j++){
				if(columnNames[j].equals(ProjectsHelper.PROJECT_NAME)){
					projects[i] = new ProjectListItem(cursor.getString(j));
					i++;
				}
			}
		}
		
		cursor.close();
		
		return projects;
	}
	
	public void insert(SQLiteDatabase db, Context context, ProjectListItem[] newProjects){
		
		db.beginTransaction();
		
		try {
			ContentValues values;
			
			for(int i = 0; i < newProjects.length; i++){
				
				values = new ContentValues();
				
				values.put(ProjectsHelper.PROJECT_NAME, newProjects[i].getProjectName());
				
				db.insert(PROJECTS_TABLE_NAME, null, values);
			}
			
			db.setTransactionSuccessful();
			
			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ProjectsListLoader.PROJECT_LIST_UPDATED));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void delete(SQLiteDatabase db, Context context,
			ProjectListItem[] list) {
		// TODO Auto-generated method stub
		
	}
	
	
}
