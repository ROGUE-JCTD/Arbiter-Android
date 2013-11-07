package com.lmn.Arbiter_Android.DatabaseHelpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ServersHelper;

public class ApplicationDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "arbiter_global.db";
	private static int DATABASE_VERSION = 1;
	
	private ApplicationDatabaseHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	private static ApplicationDatabaseHelper helper = null;
	
	public static ApplicationDatabaseHelper getHelper(Context context){
		if(helper == null){
			helper = new ApplicationDatabaseHelper(context);
		}
		
		return helper;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		ProjectsHelper.getProjectsHelper().createTable(db);
		ServersHelper.getServersHelper().createTable(db);
		LayersHelper.getLayersHelper().createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO: Migrate the tables
		db.execSQL("DROP TABLE IF EXISTS " + ProjectsHelper.PROJECTS_TABLE_NAME + ";");
		db.execSQL("DROP TABLE IF EXISTS " + ServersHelper.SERVERS_TABLE_NAME + ";");
		db.execSQL("DROP TABLE IF EXISTS " + LayersHelper.LAYERS_TABLE_NAME);
		ProjectsHelper.getProjectsHelper().createTable(db);
		ServersHelper.getServersHelper().createTable(db);
		LayersHelper.getLayersHelper().createTable(db);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db){
		super.onOpen(db);
		if(!db.isReadOnly()){
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}
}
