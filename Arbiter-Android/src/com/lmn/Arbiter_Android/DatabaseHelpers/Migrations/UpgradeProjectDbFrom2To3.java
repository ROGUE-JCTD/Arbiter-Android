package com.lmn.Arbiter_Android.DatabaseHelpers.Migrations;

import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.NotificationsTableHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.SyncTableHelper;

import android.database.sqlite.SQLiteDatabase;

public class UpgradeProjectDbFrom2To3 implements Migration{
	
	public UpgradeProjectDbFrom2To3(){
		
	}
	
	public void migrate(SQLiteDatabase db){
		
		(new SyncTableHelper(db)).createTable();
		(new NotificationsTableHelper(db)).createTable();
	}
}
