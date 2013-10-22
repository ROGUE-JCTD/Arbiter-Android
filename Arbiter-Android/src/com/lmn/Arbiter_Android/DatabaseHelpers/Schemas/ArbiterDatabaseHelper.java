package com.lmn.Arbiter_Android.DatabaseHelpers.Schemas;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public interface ArbiterDatabaseHelper<TableListItem, List> {
	
	/**
	 * Create the table
	 */
	public void createTable(SQLiteDatabase db);
	
	/**
	 * Get all of the rows in the table
	 * @return
	 */
	public TableListItem[] getAll(SQLiteDatabase db);
	
	/**
	 * Insert the item into the table
	 * @param item
	 */
	public void insert(SQLiteDatabase db, Context context, List item);
}
