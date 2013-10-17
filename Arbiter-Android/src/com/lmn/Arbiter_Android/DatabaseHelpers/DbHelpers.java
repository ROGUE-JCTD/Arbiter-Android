package com.lmn.Arbiter_Android.DatabaseHelpers;

import android.content.Context;

public class DbHelpers {
	private GlobalDatabaseHelper globalHelper;
	
	private static DbHelpers dbHelpers = null;
	
	/**
	 * Ensure that this can't be initialized more than once
	 * @param context Context of the application for the DbHelpers
	 */
	private DbHelpers(Context context){
		globalHelper = new GlobalDatabaseHelper(context);
	}
	
	public static DbHelpers getDbHelpers(Context context){
		if(dbHelpers == null){
			dbHelpers = new DbHelpers(context);
		}
		
		return dbHelpers;
	}
	
	public GlobalDatabaseHelper getGlobalDbHelper(){
		return globalHelper;
	}
}
