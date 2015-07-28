package com.lmn.Arbiter_Android.DatabaseHelpers;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lmn.Arbiter_Android.DatabaseHelpers.Migrations.Migration;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FailedSync;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.NotificationsTableHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.SyncTableHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TileIdsHelper;

public class ProjectDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "arbiter_project.db";
	private static int DATABASE_VERSION = 5;
	
	private String currentPath;
	
	private ProjectDatabaseHelper(Context context, String path){
		super(context, path + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
		this.currentPath = path;
	}
	
	private static ProjectDatabaseHelper helper = null;
	
	public static ProjectDatabaseHelper getHelper(Context context, String path, boolean reset){
		if(helper != null && 
				// path to the db isn't the same or resetting 
				(!path.equals(helper.getCurrentPath()) || reset)){
			
			resetConnection(context, path);
		}else if(helper == null){
			helper = new ProjectDatabaseHelper(context, path);
		}
		
		return helper;
	}
	
	@Override
	public void close() {
		super.close();
		
		helper = null;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		LayersHelper.getLayersHelper().createTable(db);
		PreferencesHelper.getHelper().createTable(db);
		FailedSync.getHelper().createTable(db);
		
		(new SyncTableHelper(db)).createTable();
		(new NotificationsTableHelper(db)).createTable();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		int version = oldVersion;
		int updatedVersion = newVersion;
		
		while(version != updatedVersion){
			
			try {
				Class<?> clazz = Class.forName("com.lmn.Arbiter_Android.DatabaseHelpers.Migrations.UpgradeProjectDbFrom" 
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
	
	private static void resetConnection(Context context, String path){
		if(helper != null){
			helper.close();
			helper = new ProjectDatabaseHelper(context, path);
		}
	}
	
	private String getCurrentPath(){
		return this.currentPath;
	}
}
