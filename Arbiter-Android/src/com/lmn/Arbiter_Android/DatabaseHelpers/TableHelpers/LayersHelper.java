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
					PROJECT_ID + " TEXT, " +
					SERVER_ID + " INTEGER, " + 
					" FOREIGN KEY (" + SERVER_ID + ") REFERENCES " + 
					ServersHelper.SERVERS_TABLE_NAME + " (" + ServersHelper._ID + ") " +
					" FOREIGN KEY (" + PROJECT_ID + ") REFERENCES " +
					ProjectsHelper.PROJECTS_TABLE_NAME + " (" + ProjectsHelper._ID + "));";
		
		db.execSQL(sql);
		
		createDeleteLayersByProjectTrigger(db);
		createDeleteLayersByServerTrigger(db);
	}
	
	private void createDeleteLayersByProjectTrigger(SQLiteDatabase db){
		db.execSQL("CREATE TRIGGER delete_layers_by_project BEFORE DELETE ON " +
					ProjectsHelper.PROJECTS_TABLE_NAME + " FOR EACH ROW BEGIN " +
					"DELETE FROM " + LAYERS_TABLE_NAME + " WHERE " + 
					PROJECT_ID + " = " + "OLD." + ProjectsHelper._ID + "; END;");
	}
	
	private void createDeleteLayersByServerTrigger(SQLiteDatabase db){
		db.execSQL("CREATE TRIGGER delete_layers_by_server BEFORE DELETE ON " +
					ServersHelper.SERVERS_TABLE_NAME + " FOR EACH ROW BEGIN " +
					"DELETE FROM " + LAYERS_TABLE_NAME + " WHERE " + 
					SERVER_ID + " = " + "OLD." + ServersHelper._ID + "; END;");
	}
	
	public Layer[] getAll(SQLiteDatabase db, long projectId){
		// Projection - columns to get back
		String[] columns = {
			LAYERS_TABLE_NAME + "." + _ID, // 0
			FEATURE_TYPE, // 1
			SERVER_ID, // 2
			ServersHelper.SERVER_NAME, // 3
			ServersHelper.SERVER_URL, // 4
			LAYER_TITLE, // 5
			LAYER_SRS, // 6
			BOUNDING_BOX // 7
		};
		
		// How to sort the results
		String orderBy = LAYER_TITLE + " COLLATE NOCASE";
				
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		builder.setTables(LAYERS_TABLE_NAME + " INNER JOIN " + 
				ServersHelper.SERVERS_TABLE_NAME + " ON " + LAYERS_TABLE_NAME + "." + 
				SERVER_ID + " = " + ServersHelper.SERVERS_TABLE_NAME + "." + ServersHelper._ID);
		
		String where = LAYERS_TABLE_NAME + "." + PROJECT_ID + "=?";
		String[] whereArgs = { Long.toString(projectId) };
		
		Cursor cursor = builder.query(db, columns, where, whereArgs, null, null, orderBy);
		
		Log.w("LAYERS HELPER", "GET LAYER COUNT: " + cursor.getCount());
		
		Layer[] layers = new Layer[cursor.getCount()];
		
		int i = 0;
		
		//Traverse the cursors to populate the projects array
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			layers[i] = new Layer(cursor.getInt(0),
					cursor.getString(1), cursor.getInt(2), cursor.getString(3),
					cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7));
			i++;
		}
		
		cursor.close();
		return layers;
	}
	
	public long[] insert(SQLiteDatabase db, Context context, ArrayList<Layer> newLayers, long projectId){
		db.beginTransaction();
		
		long[] layerIds = new long[newLayers.size()];
		
		try {
			ContentValues values;
			boolean somethingWentWrong = false;
			int i;
			
			for(i = 0; i < newLayers.size(); i++){
				values = new ContentValues();
				values.put(LAYER_TITLE, newLayers.get(i).getLayerTitle());
				values.put(SERVER_ID, newLayers.get(i).getServerId());
				values.put(FEATURE_TYPE, newLayers.get(i).getFeatureType());
				values.put(BOUNDING_BOX, newLayers.get(i).getLayerBBOX());
				values.put(LAYER_SRS, newLayers.get(i).getLayerSRS());
				values.put(PROJECT_ID, projectId);
				
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
		Log.w("LAYERSHELPER", "LAYERSHELPER delete");
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
}
