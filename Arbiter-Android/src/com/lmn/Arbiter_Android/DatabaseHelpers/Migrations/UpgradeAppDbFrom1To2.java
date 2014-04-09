package com.lmn.Arbiter_Android.DatabaseHelpers.Migrations;

import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;

import android.database.sqlite.SQLiteDatabase;

public class UpgradeAppDbFrom1To2 implements Migration{
	
	public void migrate(SQLiteDatabase db){
		
		PreferencesHelper.getHelper().createTable(db);
	}
}
