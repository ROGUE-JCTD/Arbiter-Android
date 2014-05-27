package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.BaseClasses.GeometryColumn;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.Loaders.LayersListLoader;
import com.lmn.Arbiter_Android.Loaders.NotificationsLoader;

public class LayersHelper implements BaseColumns{
	public static final String LAYERS_TABLE_NAME = "layers";
	public static final String LAYER_TITLE = "layer_title";
	// Feature type with prefix ex. geonode:roads
	public static final String FEATURE_TYPE = "feature_type"; 
	public static final String SERVER_ID = "server_id";
	public static final String BOUNDING_BOX = "bbox";
	public static final String COLOR = "color";
	public static final String LAYER_VISIBILITY = "visibility";
	public static final String WORKSPACE = "workspace";
	public static final String LAYER_ORDER = "layerOrder";
	public static final String READ_ONLY = "readOnly";
	public static final String ADD_LAYER_ORDER_TRIGGER_NAME = "addLayerOrder";
	
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
					BOUNDING_BOX + " TEXT, " +
					COLOR + " TEXT, " +
					LAYER_VISIBILITY + " TEXT, " +
					WORKSPACE + " TEXT, " +
					SERVER_ID + " INTEGER, " + 
					LAYER_ORDER + " INTEGER, " +
					READ_ONLY + " TEXT DEFAULT 'false');";
		
		db.execSQL(sql);
		
		createAutoIncrementLayerOrderTrigger(db);
	}
	
	private void createAutoIncrementLayerOrderTrigger(SQLiteDatabase db){
		
		String createTriggerSql = "CREATE TRIGGER " + ADD_LAYER_ORDER_TRIGGER_NAME + " "
				+ "AFTER INSERT ON " + LAYERS_TABLE_NAME + " "
				+ "BEGIN "
					+ "UPDATE " + LAYERS_TABLE_NAME + " SET " + LAYER_ORDER + "="
					+ "(SELECT IFNULL(MAX(" + LAYER_ORDER + "),0) + 1 FROM "
					+ LAYERS_TABLE_NAME + ") WHERE " + _ID + "= NEW." + _ID + "; "
				+ "END;";
		
		db.execSQL(createTriggerSql);
	}
	
	public ArrayList<Layer> getAll(SQLiteDatabase db){
		Util util = new Util();
		
		// Projection - columns to get back
		String[] columns = {
			LAYERS_TABLE_NAME + "." + _ID, // 0
			FEATURE_TYPE, // 1
			WORKSPACE, // 2
			SERVER_ID, // 3
			LAYER_TITLE, // 4
			BOUNDING_BOX, // 5
			COLOR, // 6
			LAYER_ORDER, // 7
			LAYER_VISIBILITY, // 8
			READ_ONLY // 9
		};
		
		// get all of the layers and 
		// How to sort the results
		String orderBy = LAYER_ORDER + " DESC";
		
		Cursor cursor = db.query(LAYERS_TABLE_NAME, columns, null, null, null, null, orderBy);
		
		// Create an array list with initial capacity equal to the number of layers +1 for the default layer
		ArrayList<Layer> layers = new ArrayList<Layer>(cursor.getCount() + 1);
		
		//Traverse the cursors to populate the projects array
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			layers.add(new Layer(cursor.getInt(0),
					cursor.getString(1), cursor.getString(2), cursor.getInt(3), null, null, cursor.getString(4), 
					cursor.getString(5), cursor.getString(6), cursor.getInt(7),
					util.convertIntToBoolean(cursor.getInt(8)), cursor.getString(9)));
		}
		
		cursor.close();
		
		return layers;
	}
	
	public Layer get(SQLiteDatabase db, int layerId){
		Layer layer = null;
		
		// Projection - columns to get back
		String[] columns = {
			LAYERS_TABLE_NAME + "." + _ID, // 0
			FEATURE_TYPE, // 1
			WORKSPACE, // 2
			SERVER_ID, // 3
			LAYER_TITLE, // 4
			BOUNDING_BOX, // 5
			COLOR, // 6
			LAYER_ORDER, // 7
			LAYER_VISIBILITY, // 8
			READ_ONLY // 9
			
		};
		
		String selection = _ID + "=?";
		
		String[] selectionArgs = {
			Integer.toString(layerId)
		};
		
		Cursor cursor = db.query(LAYERS_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
			
		if(cursor.moveToFirst()){
			
			Util util = new Util();
			
			layer = new Layer(cursor.getInt(0),
					cursor.getString(1), cursor.getString(2), cursor.getInt(3), null, null, cursor.getString(4), 
					cursor.getString(5), cursor.getString(6), cursor.getInt(7),
					util.convertIntToBoolean(cursor.getInt(8)), cursor.getString(9));
		}
		
		cursor.close();
		
		return layer;
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
				values.put(COLOR, layer.getColor());
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
	public void delete(SQLiteDatabase projectDb, SQLiteDatabase featureDb, Context context, Layer layer) {
		projectDb.beginTransaction();
		
		try {
			
			String featureType = layer.getFeatureTypeNoPrefix();
			
			// Remove the featureType from the geometryColumns table
			// and drop the schema table for the feature type
			GeometryColumnsHelper.getHelper().remove(
					featureDb, featureType);
			
			FailedSync.getHelper().remove(projectDb, layer.getLayerId());
			
			FailedSync.getHelper().removeFromMediaToSend(context, projectDb, layer.getLayerId());
			
			String whereClause = _ID + "=?";
			String[] whereArgs = {
				Long.toString(layer.getLayerId())
			};
			
			projectDb.delete(LAYERS_TABLE_NAME, whereClause, whereArgs);
			
			projectDb.setTransactionSuccessful();
			
			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(LayersListLoader.LAYERS_LIST_UPDATED));
			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(NotificationsLoader.NOTIFICATIONS_UPDATED));
			
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			projectDb.endTransaction();
		}
		
	}
	
	public void deleteByServerId(SQLiteDatabase projectDb, SQLiteDatabase featureDb, Context context, long serverId){
		projectDb.beginTransaction();
		
		try {
			// Projection - columns to get back
			String[] columns = {
				LAYERS_TABLE_NAME + "." + _ID, // 0
				FEATURE_TYPE, // 1
				SERVER_ID // 2
			};
			
			String where = SERVER_ID + "=?";
			String[] whereArgs = {
				Long.toString(serverId)	
			};
			
			Cursor cursor = projectDb.query(LAYERS_TABLE_NAME, 
					columns, where, whereArgs, null, null, null);
			
			//Traverse the cursors to populate the projects array
			for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				delete(projectDb, featureDb, context, new Layer(cursor.getInt(0),
						cursor.getString(1), null, cursor.getInt(2), null, null, null, 
						null, null, -1, false, "false"));
			}
			
			cursor.close();
			
			projectDb.setTransactionSuccessful();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			projectDb.endTransaction();
		}
	}
	
	private ContentValues getValuesFromLayer(Layer layer){
		
		ContentValues values = new ContentValues();
		
		values.put(LAYER_TITLE, layer.getLayerTitle());
		values.put(FEATURE_TYPE, layer.getFeatureType());
		values.put(BOUNDING_BOX, layer.getLayerBBOX());
		values.put(COLOR, layer.getColor());
		values.put(LAYER_VISIBILITY, layer.isChecked());
		values.put(WORKSPACE, layer.getWorkspace());
		values.put(SERVER_ID, layer.getServerId());
		values.put(LAYER_ORDER, layer.getLayerOrder());
		
		return values;
	}
	
	public void updateLayers(SQLiteDatabase db, Context context, ArrayList<Layer> layers){
		db.beginTransaction();
		
		try {
			
			Layer layer = null;
			
			for(int i = 0, count = layers.size(); i < count; i++){
				layer = layers.get(i);
				
				updateAttributeValues(db, context, layer.getLayerId(),
						getValuesFromLayer(layer), null);
			}
			
			db.setTransactionSuccessful();
				
			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(LayersListLoader.LAYERS_LIST_UPDATED));
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
	
	private ArrayList<Layer> addServerInfo(ArrayList<Layer> layers, SparseArray<Server> servers){
		Layer layer;
		
		for(int i = 0, count = layers.size(); i < count; i++){
			layer = layers.get(i);
			layer.setServerName(servers.get(layer.getServerId()).getName());
		}
		
		return layers;
	}
	
	private ArrayList<Layer> removeReadOnlyLayers(SQLiteDatabase featureDb, ArrayList<Layer> layers){
		
		// Layers entered into the geometry columns table are editable, so 
		// get the table entries and remove a layer from the list of layers
		// if they're not in the geometry columns table.
		HashMap<String, GeometryColumn> geometryColumns = GeometryColumnsHelper.getHelper().getAll(featureDb);
		
		Layer layer;
		
		for(int count = layers.size(), i = count - 1, j = 0; i > j; i--){
			layer = layers.get(i);
			if(!geometryColumns.containsKey(layer.getFeatureType())){
				layers.remove(i);
			}
		}
		
		return layers;
	}
	
	public ArrayList<Layer> getEditableLayers(SQLiteDatabase appDb, 
			SQLiteDatabase projectDb, SQLiteDatabase featureDb){
		
		SparseArray<Server> servers = 
				ServersHelper.getServersHelper().getAll(appDb);
		
		ArrayList<Layer> layers = getAll(projectDb);
		
		layers = addServerInfo(layers, servers);
		
		return removeReadOnlyLayers(featureDb, layers);
	}
}
