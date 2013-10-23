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
	 * Insert the list of items into the table
	 * @param db Database that the table is in
	 * @param context Context to send broadcasts
	 * @param list The list of items to be inserted
	 */
	public void insert(SQLiteDatabase db, Context context, List list);
	
	/**
	 * Delete the items in the list from the table
	 * @param db Database that the table is in
	 * @param context Context to send broadcasts
	 * @param list The list of items to be deleted
	 */
	public void delete(SQLiteDatabase db, Context context, List list);
}
