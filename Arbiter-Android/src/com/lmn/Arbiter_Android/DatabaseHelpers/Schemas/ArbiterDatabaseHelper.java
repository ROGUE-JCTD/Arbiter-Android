package com.lmn.Arbiter_Android.DatabaseHelpers.Schemas;

public interface ArbiterDatabaseHelper<TableListItem> {
	
	/**
	 * Get all of the rows in the table
	 * @return
	 */
	public TableListItem[] getAll();
	
	/**
	 * Insert the item into the table
	 * @param item
	 */
	public void insert(TableListItem item);
}
