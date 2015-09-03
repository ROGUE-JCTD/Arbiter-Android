package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import java.io.File;
import java.util.ArrayList;

import com.lmn.Arbiter_Android.Activities.HasThreadPool;
import com.lmn.Arbiter_Android.BaseClasses.BaseLayer;
import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.ConnectivityListeners.ConnectivityListener;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.FileDownloader.DownloadListener;
import com.lmn.Arbiter_Android.DatabaseHelpers.FileDownloader.FileDownloader;
import com.lmn.Arbiter_Android.Dialog.Dialogs.ChooseBaseLayer.ChooseBaselayerDialog;
import com.lmn.Arbiter_Android.Dialog.Dialogs.TilesetsDialog;
import com.lmn.Arbiter_Android.Loaders.TilesetsListLoader;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;
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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.pgsqlite.SQLitePlugin;

public class TilesetsHelper {
    public static final String TABLE_NAME = "tilesets";
    public static final String TILESET_NAME = "tileset_name";
    public static final String TIME_CREATED = "time_created";
    public static final String CREATED_BY = "created_by";
    public static final String FILESIZE = "file_size";
    public static final String BOUNDS = "bounds";
    public static final String LAYER_NAME = "layer_name";
    public static final String LAYER_ZOOM_START = "layer_zoom_start";
    public static final String LAYER_ZOOM_STOP = "layer_zoom_stop";
    public static final String RESOURCE_URI = "resource_uri";
    public static final String SERVICE_TYPE = "service_type";
    public static final String DOWNLOAD_URL = "download_url";

    public static final String SERVER_URL = "server_url";
    public static final String SERVER_USERNAME = "server_username";
    public static final String SERVER_ID = "server_id";

    public static final String FILE_LOCATION = "file_location";
    public static final String IS_DOWNLOADING = "is_downloading";

    public static final String TILESET_EXT = ".mbtiles";

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
        String URL;
        String output;

        for (int i = 0; i < tilesetsInProject.size(); i++) {
            final Tileset tileset = tilesetsInProject.get(i);

            // Check if was previously downloading
            if (tileset.getIsDownloading()) {

                // Restart downloading
                tileset.setDownloadProgress(0);

                URL = tileset.getDownloadURL();
                output = ProjectStructure.getTileSetsRoot();
                output += tileset.getTilesetName() + TILESET_EXT;

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
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                // In case of Error
                                delete(activity, tileset);
                            }
                        });
            }
        }
    }

    public void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" +
                TILESET_NAME + " TEXT NOT NULL, " +
                TIME_CREATED + " TEXT NOT NULL, " +
                CREATED_BY + " TEXT NOT NULL, " +
                FILESIZE + " INTEGER NOT NULL, " +
                BOUNDS + " TEXT NOT NULL, " +
                LAYER_NAME + " TEXT NOT NULL, " +
                LAYER_ZOOM_START + " INTEGER NOT NULL, " +
                LAYER_ZOOM_STOP + " INTEGER NOT NULL, " +
                RESOURCE_URI + " TEXT NOT NULL, " +
                SERVICE_TYPE + " TEXT NOT NULL, " +
                DOWNLOAD_URL + " TEXT NOT NULL, " +
                SERVER_ID + " INTEGER NOT NULL, " +
                SERVER_URL + " TEXT NOT NULL, " +
                SERVER_USERNAME + " TEXT NOT NULL, " +
                IS_DOWNLOADING + " INTEGER NOT NULL, " +
                FILE_LOCATION + " TEXT NOT NULL);";

        db.execSQL(sql);
    }

    public void delete(Activity activity, Tileset tileset) {
        Context context = activity.getApplicationContext();

        SQLiteDatabase appDb = ApplicationDatabaseHelper.
                getHelper(context).getWritableDatabase();

        appDb.beginTransaction();

        try {

            String whereClause = TILESET_NAME + "=?";
            String[] whereArgs = {tileset.getTilesetName()};

            // Remove from tilesets in project
            for (int i = 0; i < tilesetsInProject.size(); i++){
                if (tilesetsInProject.get(i).getTilesetName().equals(tileset.getTilesetName())){
                    tilesetsInProject.remove(i);
                }
            }

            // Delete the tileset from DB
            appDb.delete(TABLE_NAME, whereClause, whereArgs);

            // Stop downloading file
            tileset.setIsDownloading(false);

            // Delete downloaded file
            String Location = ProjectStructure.getTileSetsRoot();
            Location += tileset.getTilesetName() + TILESET_EXT;
            File file = new File(Location);
            if (file.exists())
                file.delete();

            // Remove from list of Tilesets
            SQLitePlugin.removeDBFromMap(tileset.getTilesetName() + TILESET_EXT);

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
            Tileset tileset = newTileset;

            boolean somethingWentWrong = false;

            values = getValuesFromTileset(newTileset);

            String testIfInDb = "SELECT " + TILESET_NAME + " FROM " + TABLE_NAME
                    + " WHERE " + TILESET_NAME + "=" + "\"" + tileset.getTilesetName() + "\"";
            Cursor inDbCheck = db.rawQuery(testIfInDb, null);

            boolean foundCopy = false;
            if (inDbCheck.moveToFirst()) {
                for (int j = 0; j < inDbCheck.getCount(); j++) {
                    if (inDbCheck.getString(j).equals(tileset.getTilesetName())) {
                        foundCopy = true;
                        Log.w("TilesetHelper",
                                "Found copy of tileset.. " +
                                        "Database: " + inDbCheck.getString(j) + ", " +
                                        "newTileset: " + newTileset.getTilesetName());

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

        values.put(TILESET_NAME, tileset.getTilesetName());
        values.put(TIME_CREATED, tileset.getCreatedTime());
        values.put(CREATED_BY, tileset.getCreatedBy());
        values.put(FILESIZE, tileset.getFilesize());
        values.put(BOUNDS, tileset.getGeom());
        values.put(LAYER_NAME, tileset.getLayerName());
        values.put(LAYER_ZOOM_START, tileset.getLayerZoomStart());
        values.put(LAYER_ZOOM_STOP, tileset.getLayerZoomStop());
        values.put(RESOURCE_URI, tileset.getResourceURI());
        values.put(SERVICE_TYPE, tileset.getServerServiceType());
        values.put(DOWNLOAD_URL, tileset.getDownloadURL());
        values.put(SERVER_URL, tileset.getServerURL());
        values.put(SERVER_USERNAME, tileset.getServerUsername());
        values.put(SERVER_ID, tileset.getServerID());
        values.put(FILE_LOCATION, tileset.getFileLocation());
        if (!tileset.getIsDownloading())
            values.put(IS_DOWNLOADING, 0);
        else
            values.put(IS_DOWNLOADING, 1);

        return values;
    }

    public void update(SQLiteDatabase db, Context context, Tileset tileset){
        db.beginTransaction();

        try {

            updateAttributeValues(db, tileset.getTilesetName(),
                    getValuesFromTileset(tileset), null);


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
        //StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());

        //if (VERSION.SDK_INT >= 18)
        return Environment.getExternalStorageDirectory().getUsableSpace();

       // return stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        //else
        //    return (long)stat.getAvailableBlocks() * (long)stat.getBlockSize();
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

    public void noConnectionTilesetDialog(Activity activity){
        // Here in case this is called before Init
        tilesetsInProject = getAll(ApplicationDatabaseHelper.getHelper(activity.getApplicationContext()).getReadableDatabase());

        if (tilesetsInProject.size() <= 0) {
            Context context = activity;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setTitle(R.string.error);
            String errorMsg = context.getString(R.string.tileset_no_connection_msg);
            builder.setMessage(errorMsg);
            builder.setIcon(context.getResources().getDrawable(R.drawable.icon));
            builder.setPositiveButton(android.R.string.ok, null);

            builder.create().show();
        }
    }

    public void newProjectTilesetsDialog(final Activity activity, final boolean newProject,
                                         final ConnectivityListener connectivityListener, final HasThreadPool hasThreadPool){
        Context context = activity;
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Download Tilesets");
        builder.setIcon(context.getResources().getDrawable(R.drawable.icon));

        String DownloadedStr = context.getString(R.string.tileset_new_project_msg_downloaded);
        String ConfirmationStr = context.getString(R.string.tileset_new_project_msg_confirmation);

        String Msg = DownloadedStr + " " + tilesetsInProject.size() + ConfirmationStr;

        builder.setMessage(Msg);

        builder.setNegativeButton("Skip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Go to Base Layer
                String title = activity.getResources().getString(R.string.choose_baselayer);
                String ok = activity.getResources().getString(android.R.string.ok);
                String cancel = activity.getResources().getString(android.R.string.cancel);

                ChooseBaselayerDialog newDialog = ChooseBaselayerDialog.newInstance(title, ok, cancel, R.layout.choose_baselayer_dialog,
                        newProject, BaseLayer.createOSMBaseLayer(), connectivityListener, hasThreadPool);

                FragmentActivity fragActivity = (FragmentActivity)activity;
                newDialog.show(fragActivity.getSupportFragmentManager(), ChooseBaselayerDialog.TAG);
            }
        });

        builder.setPositiveButton(R.string.tileset_dialog_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Go to Tilesets
                TilesetsDialog newDialog = TilesetsDialog.newInstance(activity.getString(R.string.tileset_dialog_title),
                        activity.getString(R.string.done), R.layout.tilesets_dialog, newProject, connectivityListener, hasThreadPool);

                FragmentActivity fragActivity = (FragmentActivity)activity;
                newDialog.show(fragActivity.getSupportFragmentManager(), "tilesetDialog");

                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    public void badFileDialog(final Activity activity, final String tilesetName){
        Context context = activity;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.error);
        builder.setIcon(context.getResources().getDrawable(R.drawable.icon));

        String errorStr = context.getResources().getString(R.string.tileset_zero_filesize);
        String errorMsg = tilesetName + " " + errorStr;

        builder.setMessage(errorMsg);
        builder.setNegativeButton(android.R.string.ok, null);
        builder.create().show();
    }

    public void serverNoTilesetsResponseDialog(Context context, String serverName){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.no_available_tileset_title);

        String errorMsg = context.getString(R.string.no_available_tileset_msg);
        builder.setMessage(serverName + " " + errorMsg);
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
                BOUNDS, // 4
                LAYER_NAME, // 5
                LAYER_ZOOM_START, // 6
                LAYER_ZOOM_STOP, // 7
                RESOURCE_URI, // 8
                SERVICE_TYPE, // 9
                DOWNLOAD_URL, // 10
                SERVER_ID, // 11
                SERVER_URL, // 12
                SERVER_USERNAME, // 13
                IS_DOWNLOADING, // 14
                FILE_LOCATION, // 15
        };

        // get all of the tilesets and
        // How to sort the results
        String orderBy = TILESET_NAME + " COLLATE NOCASE";

        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, orderBy);

        // Create an array list with initial capacity equal to the number of tilesets +1 for the default tileset
        ArrayList<Tileset> tilesets = new ArrayList<Tileset>(cursor.getCount() + 1);

        //Traverse the cursors to populate the projects array
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            tilesets.add(new Tileset(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                    cursor.getDouble(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6), cursor.getInt(7),
                    cursor.getString(8), cursor.getString(9), cursor.getString(10),
                    cursor.getInt(11), cursor.getString(12), cursor.getString(13), cursor.getString(15)));
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
                BOUNDS, // 4
                RESOURCE_URI, // 5
                SERVICE_TYPE, // 6
                DOWNLOAD_URL, // 7
                SERVER_ID, // 8
                SERVER_URL, // 9
                SERVER_USERNAME, // 10
                IS_DOWNLOADING, // 11
                FILE_LOCATION, // 12
        };

        // get all of the tilesets and
        // How to sort the results
        String orderBy = TILESET_NAME + " COLLATE NOCASE";

        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, orderBy);

        String tilesetName;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            tilesetName = cursor.getString(0);
            if (tilesetName.equals(tileset.getTilesetName())){
                return true;
            }
        }

        return false;
    }

    public ArrayList<Tileset> getTilesetsInProject() { return tilesetsInProject; }
    public void setTilesetsInProject(ArrayList<Tileset> set) { tilesetsInProject = set; }

    public String getTilesetDownloadExtension() { return TILESET_EXT; }
}
