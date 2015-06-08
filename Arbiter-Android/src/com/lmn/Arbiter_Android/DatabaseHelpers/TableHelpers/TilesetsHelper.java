package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import java.util.ArrayList;
import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.Util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TilesetsHelper {
	public static final String TABLE_NAME = "tilesets";
	public static final String TILESET_ID = "tileset_id";
	public static final String TILESET_NAME = "tileset_name";
	public static final String TIME_CREATED = "time_created";
	public static final String CREATED_BY = "created_by";
	public static final String FILESIZE = "filesize";
	public static final String SOURCE_ID = "source_id";

	private TilesetsHelper(){}
	
	private static TilesetsHelper helper = null;
	
	public static TilesetsHelper getTilesetsHelper(){
		if(helper == null){
			helper = new TilesetsHelper();
		}
		
		return helper;
	}
	
	public void createTable(SQLiteDatabase db){
		String sql = "CREATE TABLE " + TABLE_NAME + " (" +
				TILESET_ID + " INTEGER NOT NULL, " +
				TILESET_NAME + " TEXT NOT NULL, " +
				TIME_CREATED + " INTEGER NOT NULL, " +
				CREATED_BY + " TEXT NOT NULL, " +
				FILESIZE + " INTEGER NOT NULL, " +
				SOURCE_ID + " TEXT NOT NULL);";
		
		db.execSQL(sql);
	}

	public ArrayList<Tileset> getAll(SQLiteDatabase db){
		Util util = new Util();

		// Projection - columns to get back
		String[] columns = {
				TABLE_NAME + "." + TILESET_ID, // 0
				TILESET_NAME, // 1
				TIME_CREATED, // 2
				CREATED_BY, // 3
				FILESIZE, // 4
				SOURCE_ID // 5
		};

		// get all of the layers and
		// How to sort the results
		//String orderBy = LAYER_ORDER + " DESC";

		Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);

		// Create an array list with initial capacity equal to the number of layers +1 for the default layer
		ArrayList<Tileset> tilesets = new ArrayList<Tileset>(cursor.getCount() + 1);

		//Traverse the cursors to populate the projects array
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			tilesets.add(new Tileset(cursor.getString(1), cursor.getInt(2), cursor.getString(3),
					cursor.getInt(4), cursor.getString(5)));
		}

		cursor.close();

		return tilesets;
	}
}
