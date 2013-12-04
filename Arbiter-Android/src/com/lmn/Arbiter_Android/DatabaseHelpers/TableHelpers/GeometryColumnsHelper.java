package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import java.util.HashMap;

import com.lmn.Arbiter_Android.BaseClasses.GeometryColumn;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class GeometryColumnsHelper implements BaseColumns {
	public static final String GEOMETRY_COLUMNS_TABLE_NAME = "geometry_columns";
	public static final String FEATURE_TABLE_NAME = "feature_table_name";
	public static final String FEATURE_GEOMETRY_COLUMN = "feature_geometry_column";
	public static final String FEATURE_GEOMETRY_TYPE = "feature_geometry_type";
	public static final String FEATURE_GEOMETRY_SRID = "feature_geometry_srid";
	public static final String FEATURE_ENUMERATION = "feature_enumeration";

	private GeometryColumnsHelper() {}

	private static GeometryColumnsHelper helper = null;

	public static GeometryColumnsHelper getHelper() {
		if (helper == null) {
			helper = new GeometryColumnsHelper();
		}

		return helper;
	}

	public void createTable(SQLiteDatabase db) {
		String sql = "CREATE TABLE " + GEOMETRY_COLUMNS_TABLE_NAME + " (" 
						+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
						+ FEATURE_TABLE_NAME + " TEXT, " 
						+ FEATURE_GEOMETRY_COLUMN + " TEXT, "
						+ FEATURE_GEOMETRY_TYPE + " TEXT, "
						+ FEATURE_GEOMETRY_SRID + " TEXT, " 
						+ FEATURE_ENUMERATION + " TEXT, "
						+ "UNIQUE(" + FEATURE_TABLE_NAME + "));";

		db.execSQL(sql);
	}

	public HashMap<String, GeometryColumn> getAll(SQLiteDatabase db) {
		// Projection - columns to get back
		String[] columns = { _ID, // 0
				FEATURE_TABLE_NAME, // 1
				FEATURE_GEOMETRY_COLUMN, // 2
				FEATURE_GEOMETRY_TYPE, // 3
				FEATURE_GEOMETRY_SRID, // 4
				FEATURE_ENUMERATION // 5
		};

		// How to sort the results
		String orderBy = FEATURE_TABLE_NAME + " COLLATE NOCASE";

		Cursor cursor = db.query(GEOMETRY_COLUMNS_TABLE_NAME, columns, null,
				null, null, null, orderBy);

		HashMap<String, GeometryColumn> geometryColumns = 
				new HashMap<String, GeometryColumn>(cursor.getCount());
		
		// Traverse the cursors to populate the projects array
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			geometryColumns.put(cursor.getString(1), new GeometryColumn(cursor.getLong(0),
					cursor.getString(1), cursor.getString(2),
					cursor.getString(3), cursor.getString(4),
					cursor.getString(5)));
		}

		cursor.close();

		return geometryColumns;
	}
	
	public long insert(SQLiteDatabase db, GeometryColumn geometryColumn) {
		long columnId = -2;
		
		db.beginTransaction();
		
		try{
			ContentValues values = new ContentValues();
				
			values.put(FEATURE_TABLE_NAME, geometryColumn.getFeatureType());
			values.put(FEATURE_GEOMETRY_COLUMN, geometryColumn.getGeometryColumn());
			values.put(FEATURE_GEOMETRY_TYPE, geometryColumn.getGeometryType());
			values.put(FEATURE_GEOMETRY_SRID, geometryColumn.getSRID());
			values.put(FEATURE_ENUMERATION, geometryColumn.getEnumeration());
			
			columnId = db.insert(GEOMETRY_COLUMNS_TABLE_NAME, null, values);
			
			db.setTransactionSuccessful();
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
		return columnId;
	}
	
	public int remove(SQLiteDatabase db, String featureType){
		
		db.beginTransaction();
		
		int affected = 0;
		
		try{
			String whereClause = FEATURE_TABLE_NAME + "=?";
			String[] whereArgs = {
				featureType
			};
			
			// Drop the feature type table
			db.execSQL("DROP TABLE IF EXISTS " + featureType);
			
			// Remove the feature type from the geometry columns table
			affected = db.delete(GEOMETRY_COLUMNS_TABLE_NAME, whereClause, whereArgs);
			
			// Mark transaction for commit
			db.setTransactionSuccessful();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
		return affected;
	}
}
