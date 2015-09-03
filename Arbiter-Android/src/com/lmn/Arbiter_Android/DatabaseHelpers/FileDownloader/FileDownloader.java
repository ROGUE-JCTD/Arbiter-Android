package com.lmn.Arbiter_Android.DatabaseHelpers.FileDownloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.lmn.Arbiter_Android.BaseClasses.Tileset;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesetsHelper;
import com.lmn.Arbiter_Android.R;

/**
 * Created by SBird on 6/15/15.
 */

interface OnTaskCompleted{
    void onTaskCompleted();
}

public class FileDownloader implements OnTaskCompleted{

    private Runnable runnable;
    private Runnable updater;
    private Runnable errorRun;

    private long Filesize;
    private int previousProgress;
    private String fileNameStr;
    private String fileNameStrNoExt;
    private String outputStr;
    private DownloadFileFromURL downloader;
    private FragmentActivity activity;
    private String file_url = " ";

    private boolean[] showSysMessage;

    public FileDownloader(String url, String _outputStr, FragmentActivity activity,
                          Tileset tileset, Runnable updater, Runnable runMeAfter, Runnable errorRun) {
        this.file_url = url;
        this.outputStr = _outputStr;
        this.activity = activity;
        this.Filesize = (long)tileset.getFilesize();
        this.fileNameStr = tileset.getTilesetName() + TilesetsHelper.getTilesetsHelper().getTilesetDownloadExtension();
        this.fileNameStrNoExt = tileset.getTilesetName();
        this.updater = updater;
        this.runnable = runMeAfter;
        this.errorRun = errorRun;
        this.previousProgress = 0;

        this.showSysMessage = new boolean[2];
        this.showSysMessage[0] = true;
        this.showSysMessage[1] = true;

        this.downloader = new DownloadFileFromURL();
        downloader.execute(file_url);
    }

    public void onTaskCompleted(){
        runnable.run();
    }
    public void onUpdate() { updater.run(); }

    public class DownloadFileFromURL extends AsyncTask<String, Integer, String> {

        boolean keepDownloading = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strURL) {
            int count;
                try {
                    // Open URL Connection
                    URL url = new URL(strURL[0]);
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(20000);
                    connection.setDoInput(true); //Indicates that the connection should read data
                    connection.connect();

                    // Get Length of file + Input/Output Streams
                    long lengthOfFile = Filesize;
                    InputStream inputStream = new BufferedInputStream(connection.getInputStream(), 8192);

                    // Create directory in case it isn't available
                    if (!new File(outputStr).mkdir())
                        Log.w("Path created - Tileset:", outputStr);

                    // Create file to download
                    File file = new File(outputStr + fileNameStr);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    OutputStream outputStream = new FileOutputStream(outputStr + fileNameStr);

                    byte data[] = new byte[2048];
                    long total = 0;

                    try {
                        // Write to File
                        while ((count = inputStream.read(data)) != -1 && keepDownloading) {
                            total += count;

                            int progress = (int)((total * 100) / lengthOfFile);
                            publishProgress(progress);

                            outputStream.write(data, 0, count);
                        }
                    } catch (SocketTimeoutException e) {
                        Log.w("Error downloading file", strURL[0] + " SocketTimeoutException: " + e.getMessage());

                        // Remove from database and reverse download (File is never created)
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                                dialog.setTitle(R.string.error);
                                dialog.setPositiveButton(R.string.back, null);
                                String errorMsg = activity.getResources().getString(R.string.tileset_download_error_msg) + "\nErrorType: SocketTimeoutException";
                                dialog.setMessage(errorMsg);
                                dialog.create().show();
                            }
                        });

                        if (errorRun != null){
                            errorRun.run();
                        }
                    }

                    // Cancel download?
                    if (!keepDownloading) {
                        file.deleteOnExit();
                    }

                    // Flush/Close streams
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();

                } catch (final IOException e){
                    Log.w("Error downloading file", strURL[0] + " Exception: " + e.getMessage());

                    // Remove from database and reverse download (File is never created)
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                            dialog.setTitle(R.string.error);
                            dialog.setPositiveButton(R.string.back, null);
                            String errorMsg = activity.getResources().getString(R.string.tileset_download_error_msg) + "\nErrorType: IOException";
                            dialog.setMessage(errorMsg);
                            dialog.create().show();
                        }
                    });

                    if (errorRun != null){
                        errorRun.run();
                    }
                }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // setting progress percentage
            if (keepDownloading) {
                ArrayList<Tileset> tilesets = TilesetsHelper.getTilesetsHelper().getTilesetsInProject();
                Tileset tileset;

                // Tell app that progress has been updated
                if (progress[0] > previousProgress){
                    previousProgress = progress[0];
                    Log.w("Download Progress:", fileNameStrNoExt + ": " + progress[0].toString() + "%");
                    onUpdate();

                    // Find tileset and update progress locally
                    for (int i = 0; i < tilesets.size(); i++){
                        tileset = tilesets.get(i);
                        if (tileset.getTilesetName().equals(fileNameStrNoExt)){
                            tilesets.get(i).setDownloadProgress(progress[0]);
                        }
                    }
                }

                // Show Started Download (once)
                if (showSysMessage[0]) {
                    // Show sysMessage
                    Toast sysMessage = new Toast(activity);
                    String startedDownloading = activity.getString(R.string.tileset_started_downloading_msg);
                    sysMessage.makeText(activity, startedDownloading + " " + fileNameStrNoExt + "!", Toast.LENGTH_SHORT).show();

                    showSysMessage[0] = false;
                }

                // Show finished download (once)
                if (progress[0] > 99.9 && showSysMessage[1]) {

                    // Show sysMessage
                    Toast sysMessage = new Toast(activity);
                    String finishedDownloading = activity.getString(R.string.tileset_finished_downloading_msg);
                    sysMessage.makeText(activity, finishedDownloading + " " + fileNameStrNoExt + "!", Toast.LENGTH_SHORT).show();

                    showSysMessage[1] = false;
                }
            }
        }

        @Override
        protected void onPostExecute(String file_url) {
            if (runnable != null)
                onTaskCompleted();
        }
    }
}
