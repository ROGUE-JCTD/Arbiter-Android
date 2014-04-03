package com.lmn.Arbiter_Android.DatabaseHelpers.Migrations;

import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;

import android.database.sqlite.SQLiteDatabase;

public class UpgradeAppDbToVersionTwo {

	private SQLiteDatabase db;
	
	public UpgradeAppDbToVersionTwo(SQLiteDatabase db, int oldVersion, int newVersion) throws DatabaseVersionException{
		
		if(oldVersion != 1){
			throw new DatabaseVersionException("Can't upgrade db from version: " + Integer.toString(oldVersion));
		}
		
		if(newVersion != 2){
			throw new DatabaseVersionException("Can't upgrade db to version: " + Integer.toString(newVersion));
		}
		
		this.db = db;
	}
	
	public void upgrade(){
		
		PreferencesHelper.getHelper().createTable(db);
	}
}
