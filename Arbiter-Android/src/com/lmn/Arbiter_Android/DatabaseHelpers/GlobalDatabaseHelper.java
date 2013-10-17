package com.lmn.Arbiter_Android.DatabaseHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.lmn.Arbiter_Android.DatabaseHelpers.Schemas.Projects;
import com.lmn.Arbiter_Android.Projects.ProjectListItem;

public class GlobalDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "arbiter_global.db";
	private static final String PROJECT_TABLE_NAME = "projects";
	private static int DATABASE_VERSION = 1;
	
	GlobalDatabaseHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		createProjectsTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO: Migrate the tables
		db.execSQL("DROP TABLE IF EXISTS " + PROJECT_TABLE_NAME + ";");
		createProjectsTable(db);
	}
	
	private void createProjectsTable(SQLiteDatabase db){
		String sql = "CREATE TABLE " + PROJECT_TABLE_NAME + " (" +
					BaseColumns._ID +
					" INTEGER PRIMARY KEY AUTOINCREMENT, " + 
					Projects.PROJECT_NAME + " TEXT);";
		
		db.execSQL(sql);
	}
	
	public ProjectListItem[] getProjects(){
		//Projection - columns to get back
		String[] columns = {Projects.PROJECT_NAME};
		//String orderBy = Projects.PROJECT_NAME + " NOCASE";
		
		Cursor cursor =  getWritableDatabase().query(PROJECT_TABLE_NAME, columns, 
				null, null, null, null, null);
		
		ProjectListItem[] projects = new ProjectListItem[cursor.getCount()];
		
		String[] columnNames = cursor.getColumnNames();
		Log.w("GLOBAL_DB_HELPER", "cursor count: " + cursor.getCount());
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
	
	public void createProject(String projectName){
		ContentValues values = new ContentValues();
		values.put(Projects.PROJECT_NAME, projectName);
		
		getWritableDatabase().insert(PROJECT_TABLE_NAME, null, values);
	}
}
