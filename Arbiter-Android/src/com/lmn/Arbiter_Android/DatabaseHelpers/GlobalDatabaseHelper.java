package com.lmn.Arbiter_Android.DatabaseHelpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lmn.Arbiter_Android.DatabaseHelpers.Schemas.LayersHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.Schemas.ProjectsHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.Schemas.ServersHelper;

public class GlobalDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "arbiter_global.db";
	private static int DATABASE_VERSION = 1;
	private ServersHelper serversHelper = null;
	private ProjectsHelper projectsHelper = null;
	private LayersHelper layersHelper = null;
	
	GlobalDatabaseHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		serversHelper = new ServersHelper();
		projectsHelper = new ProjectsHelper();
		layersHelper = new LayersHelper();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		projectsHelper.createTable(db);
		serversHelper.createTable(db);
		layersHelper.createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO: Migrate the tables
		db.execSQL("DROP TABLE IF EXISTS " + ProjectsHelper.PROJECTS_TABLE_NAME + ";");
		db.execSQL("DROP TABLE IF EXISTS " + ServersHelper.SERVERS_TABLE_NAME + ";");
		db.execSQL("DROP TABLE IF EXISTS " + LayersHelper.LAYERS_TABLE_NAME);
		projectsHelper.createTable(db);
		serversHelper.createTable(db);
		layersHelper.createTable(db);
	}
	
	public ServersHelper getServersHelper(){
		return this.serversHelper;
	}
	
	public ProjectsHelper getProjectsHelper(){
		return this.projectsHelper;
	}
	
	public LayersHelper getLayersHelper(){
		return this.layersHelper;
	}
}
