package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import java.io.File;
import java.util.ArrayList;

import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.FileDownloader.DownloadListener;
import com.lmn.Arbiter_Android.DatabaseHelpers.FileDownloader.FileDownloader;
import com.lmn.Arbiter_Android.Loaders.TilesetsListLoader;
import com.lmn.Arbiter_Android.R;

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
import android.os.Build.VERSION;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class TilesetsHelper {
    public static final String TABLE_NAME = "tilesets";
    public static final String TILESET_NAME = "tileset_name";
    public static final String TIME_CREATED = "time_created";
    public static final String CREATED_BY = "created_by";
    public static final String FILESIZE = "filesize";
    public static final String BOUNDS = "bounds";
    public static final String SOURCE_ID = "source_id";
    public static final String IS_DOWNLOADING = "is_downloading";
    public static final String DOWNLOAD_PROGRESS = "download_progress";

    private ArrayList<Tileset> tilesetsInProject;

    private TilesetsHelper() {
    }

    private static TilesetsHelper helper = null;

    public static TilesetsHelper getTilesetsHelper() {
        if (helper == null) {
            helper = new TilesetsHelper();

            // On startup will reflect current Database
            helper.tilesetsInProject = new ArrayList<Tileset>();
        }

        return helper;
    }

    public void Init(final Activity activity){
        tilesetsInProject = getAll(ApplicationDatabaseHelper.getHelper(activity.getApplicationContext()).getReadableDatabase());

        // Restart any downloads that may have been cancelled/interrupted
        String URL = "http://download.piriform.com/mac/CCMacSetup109.dmg";
        String output;

        for (int i = 0; i < tilesetsInProject.size(); i++) {
            final Tileset tileset = tilesetsInProject.get(i);

            if (tileset.getIsDownloading()) {
                tileset.setDownloadProgress(0);

                output = "/Arbiter/TestFolder/";
                output += tileset.getName() + ".txt";

                new FileDownloader(URL, output, (FragmentActivity) activity, tileset,
                        new Runnable() {
                            @Override
                            public void run() {
                                // Each update cycle

                                // Tell TilesetListAdapter that the download is progressing
                                DownloadListener.getListener().execute();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                // Completely finished

                                // Set finished downloading
                                tileset.setIsDownloading(false);

                                // Update tileset / add to local ArrayList
                                Context context = activity.getApplicationContext();
                                ApplicationDatabaseHelper appHelper = ApplicationDatabaseHelper.getHelper(context);
                                update(appHelper.getWritableDatabase(), context, tileset);
                            }
                        });
            }
        }
    }

    public void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" +
                TILESET_NAME + " TEXT NOT NULL, " +
                TIME_CREATED + " INTEGER NOT NULL, " +
                CREATED_BY + " TEXT NOT NULL, " +
                FILESIZE + " INTEGER NOT NULL, " +
                BOUNDS + " TEXT NOT NULL, " +
                SOURCE_ID + " TEXT NOT NULL, " +
                IS_DOWNLOADING + " INTEGER NOT NULL, " +
                DOWNLOAD_PROGRESS + " INTEGER NOT NULL);";

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

            // Remove from tilesets in project
            for (int i = 0; i < tilesetsInProject.size(); i++){
                if (tilesetsInProject.get(i).getName().equals(tileset.getName())){
                    tilesetsInProject.remove(i);
                }
            }

            // Delete the tileset from DB
            appDb.delete(TABLE_NAME, whereClause, whereArgs);

            // Stop downloading file
            tileset.setIsDownloading(false);

            // Delete downloaded file
            String envPath = Environment.getExternalStorageDirectory().toString();
            String debugLocation = "/Arbiter/TestFolder/";
            debugLocation += tileset.getName() + ".txt";
            envPath += debugLocation;
            File file = new File(envPath);
            if (file.exists())
                file.delete();

            appDb.setTransactionSuccessful();

            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(TilesetsListLoader.TILESETS_LIST_UPDATED));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            appDb.endTransaction();
        }
    }

    public long[] insert(SQLiteDatabase db, Context context, Tileset newTileset) {
        db.beginTransaction();

        long[] tilesetIds = new long[1];

        try {
            ContentValues values;
            Tileset tileset;

            boolean somethingWentWrong = false;

            values = new ContentValues();
            tileset = newTileset;

            values.put(TILESET_NAME, tileset.getName());
            values.put(TIME_CREATED, tileset.getCreatedTime());
            values.put(CREATED_BY, tileset.getCreatedBy());
            values.put(FILESIZE, tileset.getFilesize());
            values.put(BOUNDS, tileset.getBounds());
            values.put(SOURCE_ID, tileset.getSourceId());
            if (tileset.getIsDownloading() == false)
                values.put(IS_DOWNLOADING, 0);
            else
                values.put(IS_DOWNLOADING, 1);
            values.put(DOWNLOAD_PROGRESS, tileset.getDownloadProgress());

            String testIfInDb = "SELECT " + TILESET_NAME + " FROM " + TABLE_NAME + " WHERE " + TILESET_NAME + "=" + "\"" + tileset.getName() + "\"";
            Cursor inDbCheck = db.rawQuery(testIfInDb, null);

            // TODO: Make sure to test against date + bounds, only check is against names
            boolean foundCopy = false;
            if (inDbCheck.moveToFirst()) {
                for (int j = 0; j < inDbCheck.getCount(); j++) {
                    if (inDbCheck.getString(j).equals(tileset.getName())) {
                        foundCopy = true;
                        Log.w("TilesetHelper",
                                "Found copy of tileset.. " +
                                        "Database: " + inDbCheck.getString(j) + ", " +
                                        "newTileset: " + newTileset.getName());

                        break;
                    }
                    inDbCheck.moveToNext();
                }
            }

            if (!foundCopy) {
                tilesetsInProject.add(tileset);
                tilesetIds[0] = db.insert(TABLE_NAME, null, values);

                if (tilesetIds[0] == -1) {
                    somethingWentWrong = true;
                }
            } else
                tilesetIds[0] = -1;

            if (!somethingWentWrong) {
                db.setTransactionSuccessful();

                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(TilesetsListLoader.TILESETS_LIST_UPDATED));
            } else {
                Log.w("TILESETSHELPER", "TILESETSHELPER Something went wrong inserting tileset: " + newTileset);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return tilesetIds;
    }

    private ContentValues getValuesFromTileset(Tileset tileset){

        ContentValues values = new ContentValues();

        values.put(TILESET_NAME, tileset.getName());
        values.put(TIME_CREATED, tileset.getCreatedTime());
        values.put(CREATED_BY, tileset.getCreatedBy());
        values.put(FILESIZE, tileset.getFilesize());
        values.put(BOUNDS, tileset.getBounds());
        values.put(SOURCE_ID, tileset.getSourceId());
        if (!tileset.getIsDownloading())
            values.put(IS_DOWNLOADING, 0);
        else
            values.put(IS_DOWNLOADING, 1);
        values.put(DOWNLOAD_PROGRESS, tileset.getDownloadProgress());

        return values;
    }

    private ContentValues getProgressValueFromTileset(Tileset tileset){

        ContentValues values = new ContentValues();

        values.put(DOWNLOAD_PROGRESS, tileset.getDownloadProgress());

        return values;
    }

    public void update(SQLiteDatabase db, Context context, Tileset tileset){
        db.beginTransaction();

        try {

            updateAttributeValues(db, tileset.getName(),
                    getValuesFromTileset(tileset), null);


            db.setTransactionSuccessful();

            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(TilesetsListLoader.TILESETS_LIST_UPDATED));
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void updateProgress(SQLiteDatabase db, Context context, Tileset tileset){
        db.beginTransaction();

        try {

            updateProgressValue(db, tileset.getName(),
                    getProgressValueFromTileset(tileset), null);


            db.setTransactionSuccessful();

            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(TilesetsListLoader.TILESETS_LIST_UPDATED));
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void updateAttributeValues(SQLiteDatabase db, String tilesetName,
                                      ContentValues values, Runnable callback){
        db.beginTransaction();
        try {

            String whereClause = TILESET_NAME + "=?";
            String[] whereArgs = {
                    tilesetName
            };

            db.update(TABLE_NAME, values, whereClause, whereArgs);

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

    public void updateProgressValue(SQLiteDatabase db, String tilesetName,
                                      ContentValues values, Runnable callback){

        db.beginTransaction();

        try {

            String whereClause = TILESET_NAME + "=?";
            String[] whereArgs = {
                    tilesetName
            };

            db.update(TABLE_NAME, values, whereClause, whereArgs);

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

    public String convertFilesize(double number) {
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

    private long availableSpaceOnPhone() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());

        if (VERSION.SDK_INT >= 18)
            return stat.getBlockCountLong() * stat.getBlockSizeLong();
        else
            return (long)stat.getBlockCount() * (long)stat.getBlockSize();
    }

    public void notEnoughSpaceAlert(Activity activity, String tilesetName){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        String title = activity.getResources().getString(R.string.not_enough_space);
        String message1 = activity.getResources().getString(R.string.not_enough_space_msg1);
        String message2 = activity.getResources().getString(R.string.not_enough_space_msg2);

        builder.setTitle(title);
        builder.setIcon(activity.getResources().getDrawable(R.drawable.icon));
        builder.setMessage(message1 + " " + tilesetName + ". " + message2);
        builder.setPositiveButton(R.string.back, null);

        builder.create().show();
    }

    public void deletionAlert(Activity activity, final Runnable deleteIt, String tilesetName) {
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

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }

    public void cancellationAlert(Activity activity, final Runnable deleteIt, String tilesetName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(R.string.warning);
        builder.setIcon(activity.getResources().getDrawable(R.drawable.icon));
        String deleteTilesetStr = activity.getApplicationContext().getResources().getString(R.string.cancel_download_tileset_alert);
        builder.setMessage(deleteTilesetStr + " " + tilesetName + "?");
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteIt.run();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }

    public void serverResponseDialog(Context context, String serverName){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.error_retrieving_tileset);

        String errorMsg1 = context.getString(R.string.error_connecting_tileset1);
        String errorMsg2 = context.getString(R.string.error_connecting_tileset2);
        builder.setMessage(errorMsg1 + " " + serverName + ". " + errorMsg2);
        builder.setIcon(context.getResources().getDrawable(R.drawable.icon));
        builder.setPositiveButton(android.R.string.ok, null);

        builder.create().show();
    }

    public void downloadSizeDialog(final Activity activity, final Runnable downloadIt,
                                   final double filesize, final String tilesetName) {
        Context context = activity;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final long freeSpace = availableSpaceOnPhone();
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
                    notEnoughSpaceAlert(activity, tilesetName);
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

    public ArrayList<Tileset> getAll(SQLiteDatabase db) {

        // Projection - columns to get back
        String[] columns = {
                TILESET_NAME, // 0
                TIME_CREATED, // 1
                CREATED_BY, // 2
                FILESIZE, // 3
                SOURCE_ID, // 4
                BOUNDS, // 5
                IS_DOWNLOADING, // 6
                DOWNLOAD_PROGRESS // 7
        };

        // get all of the tilesets and
        // How to sort the results
        String orderBy = TILESET_NAME + " COLLATE NOCASE";

        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, orderBy);

        // Create an array list with initial capacity equal to the number of tilesets +1 for the default tileset
        ArrayList<Tileset> tilesets = new ArrayList<Tileset>(cursor.getCount() + 1);

        //Traverse the cursors to populate the projects array
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            tilesets.add(new Tileset(cursor.getString(0), cursor.getLong(1), cursor.getString(2),
                    cursor.getDouble(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6), cursor.getInt(7)));
        }

        cursor.close();

        return tilesets;
    }

    public boolean checkInDatabase(SQLiteDatabase db, Tileset tileset){

        // Projection - columns to get back
        String[] columns = {
                TILESET_NAME, // 0
                TIME_CREATED, // 1
                CREATED_BY, // 2
                FILESIZE, // 3
                SOURCE_ID, // 4
                BOUNDS, // 5
                IS_DOWNLOADING, // 6
                DOWNLOAD_PROGRESS // 7
        };

        // get all of the tilesets and
        // How to sort the results
        String orderBy = TILESET_NAME + " COLLATE NOCASE";

        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, orderBy);

        String tilesetName;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            tilesetName = cursor.getString(0);
            if (tilesetName.equals(tileset.getName())){
                return true;
            }
        }

        return false;
    }

    public ArrayList<Tileset> getTilesetsInProject() { return tilesetsInProject; }
    public void setTilesetsInProject(ArrayList<Tileset> tilesets) { tilesetsInProject = tilesets; }
}
