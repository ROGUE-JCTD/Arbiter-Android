package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.Loaders.LayersListLoader;

public class LayersHelper implements BaseColumns{
	public static final String LAYERS_TABLE_NAME = "layers";
	public static final String LAYER_TITLE = "layer_title";
	// Feature type with prefix ex. geonode:roads
	public static final String FEATURE_TYPE = "feature_type"; 
	public static final String SERVER_ID = "server_id";
	public static final String LAYER_SRS = "srs";
	public static final String BOUNDING_BOX = "bbox";
	public static final String PROJECT_ID = "project_id";
	public static final String LAYER_VISIBILITY = "visibility";
	
	private LayersHelper(){}
	
	private static LayersHelper helper = null;
	
	public static LayersHelper getLayersHelper(){
		if(helper == null){
			helper = new LayersHelper();
		}
		
		return helper;
	}
	
	public void createTable(SQLiteDatabase db){
		String sql = "CREATE TABLE " + LAYERS_TABLE_NAME + " (" +
					_ID +
					" INTEGER PRIMARY KEY AUTOINCREMENT, " +
					LAYER_TITLE + " TEXT, " +
					FEATURE_TYPE + " TEXT, " +
					LAYER_SRS + " TEXT, " +
					BOUNDING_BOX + " TEXT, " +
					LAYER_VISIBILITY + " TEXT, " +
					SERVER_ID + " INTEGER);";
		
		db.execSQL(sql);
	}
	
	public ArrayList<Layer> getAll(SQLiteDatabase db){
		// Projection - columns to get back
		String[] columns = {
			LAYERS_TABLE_NAME + "." + _ID, // 0
			FEATURE_TYPE, // 1
			SERVER_ID, // 2
			LAYER_TITLE, // 3
			LAYER_SRS, // 4
			BOUNDING_BOX, // 5
			LAYER_VISIBILITY // 6
		};
		
		// get all of the layers and 
		// How to sort the results
		String orderBy = LAYER_TITLE + " COLLATE NOCASE";
		
		Cursor cursor = db.query(LAYERS_TABLE_NAME, columns, null, null, null, null, orderBy);
		
		// Create an array list with initial capacity equal to the number of layers +1 for the default layer
		ArrayList<Layer> layers = new ArrayList<Layer>(cursor.getCount() + 1);
		
		//Traverse the cursors to populate the projects array
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			layers.add(new Layer(cursor.getInt(0),
					cursor.getString(1), cursor.getInt(2), null, null, cursor.getString(3), cursor.getString(4), 
					cursor.getString(5), Util.convertIntToBoolean(cursor.getInt(6))));
		}
		
		cursor.close();
		
		return layers;
	}
	
	public long[] insert(SQLiteDatabase db, Context context, ArrayList<Layer> newLayers){
		db.beginTransaction();
		
		long[] layerIds = new long[newLayers.size()];
		
		try {
			ContentValues values;
			Layer layer;
			
			boolean somethingWentWrong = false;
			int i;
			
			for(i = 0; i < newLayers.size(); i++){
				values = new ContentValues();
				layer = newLayers.get(i);
				values.put(LAYER_TITLE, layer.getLayerTitle());
				values.put(SERVER_ID, layer.getServerId());
				values.put(FEATURE_TYPE, layer.getFeatureType());
				values.put(BOUNDING_BOX, layer.getLayerBBOX());
				values.put(LAYER_SRS, layer.getLayerSRS());
				values.put(LAYER_VISIBILITY, layer.isChecked());
				
				layerIds[i] = db.insert(LAYERS_TABLE_NAME, null, values);
				
				if(layerIds[i] == -1){
					somethingWentWrong = true;
					break;
				}
			}
			
			if(!somethingWentWrong){
				db.setTransactionSuccessful();
				
				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(LayersListLoader.LAYERS_LIST_UPDATED));
			}else{
				Log.w("LAYERSHELPER", "LAYERSHELPER Something went wrong inserting layer: " + newLayers.get(i).getFeatureType());
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
		return layerIds;
	}

	/**
	 * Delete the list of layers
	 * @param db The database the layers reside in
	 * @param context The context to send a broadcast notifying the layers have been updated
	 * @param list The list of layers to be deleted
	 */
	public void delete(SQLiteDatabase db, Context context, Layer layer, Runnable callback) {
		db.beginTransaction();
		
		try {
			String whereClause = _ID + "=?";
			String[] whereArgs = {
				Long.toString(layer.getLayerId())
			};
			
			db.delete(LAYERS_TABLE_NAME, whereClause, whereArgs);
			
			db.setTransactionSuccessful();
			
			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(LayersListLoader.LAYERS_LIST_UPDATED));
			
			if(callback != null){
				callback.run();
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
	}
	
	public void updateAttributeValues(SQLiteDatabase db, Context context, 
			long layerId, ContentValues values, Runnable callback){
		
		db.beginTransaction();
		
		try {
			
			String whereClause = _ID + "=?";
			String[] whereArgs = {
					Long.toString(layerId)	
			};
			
			db.update(LAYERS_TABLE_NAME, values, whereClause, whereArgs);
			
			db.setTransactionSuccessful();
			
			if(callback != null){
				callback.run();
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
}
