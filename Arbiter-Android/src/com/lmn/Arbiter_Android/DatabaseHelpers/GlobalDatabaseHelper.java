package com.lmn.Arbiter_Android.DatabaseHelpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.lmn.Arbiter_Android.DatabaseHelpers.Schemas.Projects;

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
	
	
}
