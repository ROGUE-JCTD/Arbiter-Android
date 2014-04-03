package com.lmn.Arbiter_Android.DatabaseHelpers;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lmn.Arbiter_Android.DatabaseHelpers.Migrations.DatabaseVersionException;
import com.lmn.Arbiter_Android.DatabaseHelpers.Migrations.UpgradeAppDbToVersionTwo;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ServersHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class ApplicationDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "arbiter_application.db";
	private static int DATABASE_VERSION = 2;
	
	private ApplicationDatabaseHelper(Context context){
		super(context, ProjectStructure.getApplicationRoot() + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
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
		ServersHelper.getServersHelper().createTable(db);
		TilesHelper.getHelper().createTable(db);
		PreferencesHelper.getHelper().createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		if(oldVersion == 1 && newVersion == 2){
			try {
				UpgradeAppDbToVersionTwo upgrader = new UpgradeAppDbToVersionTwo(db, oldVersion, newVersion);
				
				upgrader.upgrade();
			} catch (DatabaseVersionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
