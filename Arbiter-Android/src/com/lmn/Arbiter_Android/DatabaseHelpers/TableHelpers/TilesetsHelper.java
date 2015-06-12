package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import java.util.ArrayList;
import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.Loaders.TilesetsListLoader;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class TilesetsHelper {
	public static final String TABLE_NAME = "tilesets";
	public static final String TILESET_NAME = "tileset_name";
	public static final String TIME_CREATED = "time_created";
	public static final String CREATED_BY = "created_by";
	public static final String FILESIZE = "filesize";
	public static final String BOUNDS = "bounds";
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
				TILESET_NAME + " TEXT NOT NULL, " +
				TIME_CREATED + " INTEGER NOT NULL, " +
				CREATED_BY + " TEXT NOT NULL, " +
				FILESIZE + " INTEGER NOT NULL, " +
				BOUNDS + " TEXT NOT NULL, " +
				SOURCE_ID + " TEXT NOT NULL);";
		
		db.execSQL(sql);
	}

	public void delete(Activity activity, Tileset tileset) {
		Context context = activity.getApplicationContext();

		SQLiteDatabase appDb = ApplicationDatabaseHelper.
				getHelper(context).getWritableDatabase();

		appDb.beginTransaction();

		try {

			String whereClause = TILESET_NAME + "=?";
			String[] whereArgs = {tileset.getName()};

			// Delete the tileset
			appDb.delete(TABLE_NAME, whereClause, whereArgs);

			appDb.setTransactionSuccessful();

			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(TilesetsListLoader.TILESETS_LIST_UPDATED));
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			appDb.endTransaction();
		}
	}

	public long[] insert(SQLiteDatabase db, Context context, ArrayList<Tileset> newTilesets){
		db.beginTransaction();

		long[] tilesetIds = new long[newTilesets.size()];

		try {
			ContentValues values;
			Tileset tileset;

			boolean somethingWentWrong = false;
			int i;

			for(i = 0; i < newTilesets.size(); i++) {
				values = new ContentValues();
				tileset = newTilesets.get(i);

				values.put(TILESET_NAME, tileset.getName());
				values.put(TIME_CREATED, tileset.getCreatedTime());
				values.put(CREATED_BY, tileset.getCreatedBy());
				values.put(FILESIZE, tileset.getFilesize());
				values.put(BOUNDS, tileset.getBounds());
				values.put(SOURCE_ID, tileset.getSourceId());

				String testIfInDb = "SELECT " + TILESET_NAME + " FROM " + TABLE_NAME + " WHERE " + TILESET_NAME + "=" + "\"" + tileset.getName() + "\"";
				Cursor inDbCheck = db.rawQuery(testIfInDb, null);
				boolean foundCopy = false;
				if (inDbCheck.moveToFirst()) {
					for (int j = 0; j < inDbCheck.getCount(); j++) {
						if (inDbCheck.getString(j).equals(tileset.getName())) {
							foundCopy = true;

							/*if (context != null) {
								AlertDialog.Builder existsDialog = new AlertDialog.Builder(context);
								existsDialog.setTitle("Found Copy");
								existsDialog.setIcon(context.getResources().getDrawable(R.drawable.icon));
								existsDialog.setMessage("Found " + tileset.getName() + " in TileSets already!!!");
								existsDialog.setPositiveButton("OK", null);
								existsDialog.create().show();
							}*/

							break;
						}
						inDbCheck.moveToNext();
					}
				}

				if (!foundCopy) {
					tilesetIds[i] = db.insert(TABLE_NAME, null, values);

					if (tilesetIds[i] == -1) {
						somethingWentWrong = true;
						break;
					}
				}
				else
					tilesetIds[i] = -1;
			}

			if (!somethingWentWrong) {
				db.setTransactionSuccessful();

				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(TilesetsListLoader.TILESETS_LIST_UPDATED));
			} else {
				Log.w("TILESETSHELPER", "TILESETSHELPER Something went wrong inserting tileset: " + newTilesets.get(i));
			}

		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}

		return tilesetIds;
	}

	public String convertFilesize(double number){
		// Will convert from bytes to bytes, KB, MB, or GB
		String result = "";

		if (number > 0.0) {
			if (number > 1073741824.0) {
				String num = String.format("%.2f", (number / 1073741824.0));
				result += num + "GB";
			} else if (number > 1048576.0) {
				String num = String.format("%.2f", (number / 1048576.0));
				result += num + "MB";
			} else if (number > 1024.0) {
				String num = String.format("%.2f", (number / 1024.0));
				result += num + "KB";
			} else {
				result += number + " bytes";
			}
		} else {
			if (number < -1073741824.0) {
				String num = String.format("%.2f", (number / 1073741824.0));
				result += num + "GB";
			} else if (number < -1048576.0) {
				String num = String.format("%.2f", (number / 1048576.0));
				result += num + "MB";
			} else if (number < -1024.0) {
				String num = String.format("%.2f", (number / 1024.0));
				result += num + "KB";
			} else {
				result += number + " bytes";
			}
		}

		return result;
	}

	private double availableSpaceOnPhone(){
		StatFs stat = new StatFs(Environment.getDataDirectory().getPath());

		return stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
	}

	public void deletionAlert(Activity activity, final Runnable deleteIt, String tilesetName){
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		builder.setTitle(R.string.warning);
		builder.setIcon(activity.getResources().getDrawable(R.drawable.icon));
		String deleteTilesetStr = activity.getApplicationContext().getResources().getString(R.string.delete_tileset_alert);
		builder.setMessage(deleteTilesetStr + " " + tilesetName + "?");
		builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteIt.run();
			}
		});

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		builder.create().show();
	}

	public void downloadSizeDialog(Context context, final Runnable downloadIt,
								   final double filesize){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		final double freeSpace = availableSpaceOnPhone();
		final String strFilesize = convertFilesize(filesize);
		final String strAvailableSpace = convertFilesize(freeSpace);
		final String strSpaceAfterDL = convertFilesize(freeSpace - filesize);

		builder.setTitle(R.string.downloading_tileset);
		builder.setIcon(context.getResources().getDrawable(R.drawable.icon));

		String downloadMsg = context.getResources().getString(R.string.downloading_tileset_msg);
		String downloadMsg2 = context.getResources().getString(R.string.downloading_tileset_msg2);
		String availableSpace = context.getResources().getString(R.string.space_available);
		String spaceAfterDownload = context.getResources().getString(R.string.space_after_download);

		builder.setMessage(downloadMsg + " " + strFilesize + ". \n"
				+ downloadMsg2 + "\n\n"
				+ availableSpace + " " + strAvailableSpace
				+ "\n" + spaceAfterDownload + " " + strSpaceAfterDL);

		builder.setPositiveButton(R.string.downloading_tileset, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if ((freeSpace - filesize) <= 0.0) {
					// Can't download it
					// Need AlertDialog
				} else {
					// Can download it
					downloadIt.run();
				}
			}
		});

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		builder.create().show();
	}

	public ArrayList<Tileset> getAll(SQLiteDatabase db){
		Util util = new Util();

		// Projection - columns to get back
		String[] columns = {
				TILESET_NAME, // 0
				TIME_CREATED, // 1
				CREATED_BY, // 2
				FILESIZE, // 3
				SOURCE_ID, // 4
				BOUNDS // 5
		};

		// get all of the tilesets and
		// How to sort the results
		String orderBy = TILESET_NAME + " COLLATE NOCASE";

		Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, orderBy);

		// Create an array list with initial capacity equal to the number of tilesets +1 for the default tileset
		ArrayList<Tileset> tilesets = new ArrayList<Tileset>(cursor.getCount() + 1);

		//Traverse the cursors to populate the projects array
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			tilesets.add(new Tileset(cursor.getString(0), cursor.getLong(1), cursor.getString(2),
					cursor.getDouble(3), cursor.getString(4), cursor.getString(5)));
		}

		cursor.close();

		return tilesets;
	}
}
