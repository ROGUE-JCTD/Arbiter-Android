package com.lmn.Arbiter_Android.DatabaseHelpers;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;

public class ProjectDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "arbiter_project.db";
	private static int DATABASE_VERSION = 1;
	
	private String currentPath;
	
	private ProjectDatabaseHelper(Context context, String path){
		super(context, path + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
		this.currentPath = path;
	}
	
	private static ProjectDatabaseHelper helper = null;
	
	public static ProjectDatabaseHelper getHelper(Context context, String path){
		if(helper == null){
			helper = new ProjectDatabaseHelper(context, path);
		}else{
			// If the path is different, the project got switched
			// so close the db connection, and get a new connection
			if(!path.equals(helper.getCurrentPath())){
				helper.close();
				helper = new ProjectDatabaseHelper(context, path);
			}
		}
		
		return helper;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		LayersHelper.getLayersHelper().createTable(db);
		PreferencesHelper.getHelper().createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO: Migrate the tables
		db.execSQL("DROP TABLE IF EXISTS " + LayersHelper.LAYERS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + PreferencesHelper.TABLE_NAME);
		
		LayersHelper.getLayersHelper().createTable(db);
		PreferencesHelper.getHelper().createTable(db);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db){
		super.onOpen(db);
		if(!db.isReadOnly()){
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}
	
	private String getCurrentPath(){
		return this.currentPath;
	}
}
