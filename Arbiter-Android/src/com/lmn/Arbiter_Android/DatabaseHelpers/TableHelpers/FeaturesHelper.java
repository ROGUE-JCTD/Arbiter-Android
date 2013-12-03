package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import com.lmn.Arbiter_Android.BaseClasses.Feature;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FeaturesHelper{
	private String ID = "arbiter_id";
	
	private FeaturesHelper() {}

	enum Errors {
		NO_GEOMETRY_COLUMN
	}
	
	private static FeaturesHelper helper = null;

	public static FeaturesHelper getHelper() {
		if (helper == null) {
			helper = new FeaturesHelper();
		}

		return helper;
	}

	public Feature getFeature(SQLiteDatabase db, String id, String featureType) {
		String whereClause = ID + "=?";
		String[] whereArgs = {
			id
		};
				
		String geometryColumn = null;
		
		try {
			geometryColumn = getGeometryColumn(db, featureType);
		} catch (FeatureHelperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Cursor cursor = db.query(featureType, null, whereClause,
				whereArgs, null, null, null);

		String[] columnNames = cursor.getColumnNames();
		
		ContentValues attributes = new ContentValues();
		
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			for(int i = 0; i < columnNames.length; i++){
				if(!columnNames[i].equals(ID)){
					attributes.put(columnNames[i], cursor.getString(i));
				}
			}
		}

		cursor.close();

		return new Feature(id, geometryColumn, attributes);
	}
	
	private String getGeometryColumn(SQLiteDatabase db, String featureType) throws FeatureHelperException{
		String[] columns = {
			GeometryColumnsHelper.FEATURE_GEOMETRY_COLUMN // 0	
		};
		
		String whereClause = GeometryColumnsHelper.FEATURE_TABLE_NAME + "=?";
		String[] whereArgs = {
			featureType
		};
		
		Cursor cursor = db.query(GeometryColumnsHelper.GEOMETRY_COLUMNS_TABLE_NAME, 
				columns, whereClause, whereArgs, null, null, null);
		
		String geometryColumn = null;
		
		if(cursor.moveToFirst()){
			geometryColumn = cursor.getString(0);
		}
		
		cursor.close();
		
		if(geometryColumn != null){
			return geometryColumn;
		}
		
		throw new FeatureHelperException(Errors.NO_GEOMETRY_COLUMN);
	}
	
	public void update(SQLiteDatabase db, String featureType, String id, Feature feature){
		
		db.beginTransaction();
		
		try{ 
			String whereClause = ID + "=?";
			String[] whereArgs = {
				id
			};
			
			db.update(featureType, feature.getAttributes(), whereClause, whereArgs);
			
			db.setTransactionSuccessful();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
	
	public int delete(SQLiteDatabase db, String featureType, String id){
		
		db.beginTransaction();
		
		int affected = 0;
		
		try{
			String whereClause = ID + "=?";
			String[] whereArgs = {
				id
			};
			
			// Remove the feature type from the geometry columns table
			affected = db.delete(featureType, whereClause, whereArgs);
			
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
