package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import java.util.LinkedHashMap;
import java.util.Map;

import com.lmn.Arbiter_Android.BaseClasses.Feature;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FeaturesHelper{
	public static final String ID = "arbiter_id";
	public static final String SYNC_STATE = "sync_state";
	public static final String MODIFIED_STATE = "modified_state";
	public static final String FID = "fid";
	
	public class SYNC_STATES{
		public static final String NOT_SYNCED = "0";
		public static final String SYNCED = "1";
	}
	
	public class MODIFIED_STATES{
		public static final String NONE = "0";
		public static final String INSERTED = "1";
		public static final String MODIFIED = "2";
		public static final String DELETED = "3";
	}
	
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

	public int getUnsyncedFeatureCount(SQLiteDatabase db, String featureType){
		
		String[] whereArgs = {
			SYNC_STATES.NOT_SYNCED
		};
		
		String sql = "SELECT COUNT(*) FROM " + featureType + " WHERE " + SYNC_STATE + "=?;";
		
		Cursor cursor = db.rawQuery(sql, whereArgs);
		
		int count = 0;

		if(cursor.getCount() > 0) {
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
		}
		
		cursor.close();
		
		return count;
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
		
		LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>(cursor.getColumnCount());
		
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			for(int i = 0; i < columnNames.length; i++){
				
				if(!columnNames[i].equals(ID)){
					attributes.put(columnNames[i], cursor.getString(i));
				}
			}
		}

		cursor.close();

		return new Feature(id, featureType, geometryColumn, attributes);
	}

	public boolean isInDatabase(SQLiteDatabase db, String layerName){
		boolean foundFeature = false;

		String testIfInDb = "SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = \"" + layerName + "\";";
		Cursor inDbCheck = db.rawQuery(testIfInDb, null);

		if (inDbCheck != null) {
			if (inDbCheck.getCount() > 0) {
				foundFeature = true;
			}
			inDbCheck.close();
		}

		return foundFeature;
	}
	
	public Feature getFeatureByFid(SQLiteDatabase db, String fid, String featureType){
		
		String whereClause = FID + "=?";
		String[] whereArgs = {
			fid
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
		String id = null;
		
		LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>(cursor.getColumnCount());
		
		Feature feature = null;
		
		if(cursor.moveToFirst()){
			
			for(int i = 0; i < columnNames.length; i++){
				
				if(!columnNames[i].equals(ID)){
					attributes.put(columnNames[i], cursor.getString(i));
				}else{
					id = cursor.getString(i);
				}
			}
			
			feature = new Feature(id, featureType, geometryColumn, attributes);
		}

		cursor.close();

		
		return feature;
	}
	
	private LinkedHashMap<String, String> getEmptyAttributesWithGeometry(SQLiteDatabase db, 
			String featureType, String geometryColumn, String wktGeometry){
		
		LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>();
		
		Cursor cursor = db.rawQuery("PRAGMA table_info(" + featureType + ")", null);
		
		String attrName = null;
		
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
			attrName = cursor.getString(1);
			
			if(attrName.equals(geometryColumn)){
				attributes.put(attrName, wktGeometry);
			}else if(!attrName.equals(ID)){
				attributes.put(attrName, "");
			}
		}
		
		return attributes;
	}
	
	public Feature getNewFeature(SQLiteDatabase db, String featureType, String wktGeometry){
		String geometryColumn = null;
		
		try {
			geometryColumn = getGeometryColumn(db, featureType);
			
		} catch (FeatureHelperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LinkedHashMap<String, String> attributes = getEmptyAttributesWithGeometry(db, 
				featureType, geometryColumn, wktGeometry);
		
		Log.w("FeaturesHelper", "FeaturesHelper getEmptyAttributesWithGeometry featureType " + featureType + ", wktGeometry = " + wktGeometry);
		
		return new Feature(featureType, geometryColumn, attributes);
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
	
	private ContentValues getContentValues(LinkedHashMap<String, String> map){
		ContentValues contentValues = new ContentValues();
		
		for(Map.Entry<String, String> entry : map.entrySet()){
			contentValues.put(entry.getKey(), entry.getValue());
		}
		
		return contentValues;
	}
	
	public String insert(SQLiteDatabase db, String featureType, Feature feature){
		
		String id = null;
		
		db.beginTransaction();
		
		try{
			id = Long.toString(db.insert(featureType, null, getContentValues(feature.getAttributes())));
			
			db.setTransactionSuccessful();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
		return id;
	}
	
	public void update(SQLiteDatabase db, String featureType, String id, Feature feature){
		
		db.beginTransaction();
		
		try{ 
			String whereClause = ID + "=?";
			String[] whereArgs = {
				id
			};
			
			if(feature.getModifiedState() == null){
				throw new Exception("Feature Update: modified state should not be null");
			}
			
			if(feature.getSyncState() == null){
				throw new Exception("Feature Update: sync state should not be null");
			}
			
			db.update(featureType, getContentValues(feature.getAttributes()), whereClause, whereArgs);
			
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
