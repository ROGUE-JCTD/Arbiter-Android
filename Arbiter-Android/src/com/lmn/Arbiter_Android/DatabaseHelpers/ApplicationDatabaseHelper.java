package com.lmn.Arbiter_Android.DatabaseHelpers;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lmn.Arbiter_Android.DatabaseHelpers.Migrations.Migration;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ServersHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesetsHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class ApplicationDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "arbiter_application.db";
	private static int DATABASE_VERSION = 4;
	
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
		PreferencesHelper.getHelper().createTable(db);
		TilesetsHelper.getTilesetsHelper().createTable(db);
	}

	@Override
	public void close() {
		super.close();
		
		helper = null;
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		int version = oldVersion;
		int updatedVersion = newVersion;
		
		while(version != updatedVersion){
			
			try {
				Class<?> clazz = Class.forName("com.lmn.Arbiter_Android.DatabaseHelpers.Migrations.UpgradeAppDbFrom" 
						+ Integer.toString(version) + "To" 
						+ Integer.toString(++version));
				
				Migration migration = (Migration) clazz.newInstance();
				
				migration.migrate(db);
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}catch(ClassCastException e){
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
