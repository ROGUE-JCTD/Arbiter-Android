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

    private String fileNameStr;
    private String outputStr;
    private DownloadFileFromURL downloader;
    private FragmentActivity activity;
    private String file_url = " ";

    private boolean[] showSysMessage;

    public FileDownloader(String url, String _outputStr, FragmentActivity activity, Tileset tileset, Runnable updater, Runnable runMeAfter) {
        this.file_url = url;
        this.outputStr = _outputStr;
        this.activity = activity;
        this.fileNameStr = tileset.getTilesetName();
        this.updater = updater;
        this.runnable = runMeAfter;

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

        public boolean getKeepDownloading() { return this.keepDownloading; }
        public void setKeepDownloading(boolean dl) { this.keepDownloading = dl; }

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
                    connection.setConnectTimeout(3000);
                    connection.connect();

                    // Get Length of file + Input/Output Streams
                    int lengthOfFile = connection.getContentLength();
                    InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);

                    // Create directory in case it isn't available
                    String filePath = Environment.getExternalStorageDirectory().toString();
                    if (!new File(filePath + TilesetsHelper.getTilesetsHelper().getTilesetDownloadLocation()).mkdir());
                        Log.w("Path created", "Hooray!");

                    // Create file to download
                    filePath += outputStr;
                    File file = new File(filePath);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    OutputStream outputStream = new FileOutputStream(filePath);

                    byte data[] = new byte[1024];
                    long total = 0;

                    try {
                        // Write to File
                        while ((count = inputStream.read(data)) != -1 && keepDownloading) {
                            total += count;

                            publishProgress((int) ((total * 100) / lengthOfFile));

                            outputStream.write(data, 0, count);
                        }
                    } catch (SocketTimeoutException e) {
                        Log.w("Error downloading file", strURL[0] + " SocketException: " + e.getMessage());
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

                    // TODO: Remove from database and reverse download (File is never created)
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                            dialog.setTitle(R.string.error);
                            dialog.setMessage("There was a problem with the download: " + e.getMessage() +
                                    "\n\nThe file was not downloaded. Please delete and try again.");
                            dialog.create().show();
                        }
                    });
                }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // setting progress percentage
            if (keepDownloading) {
                ArrayList<Tileset> tilesets = TilesetsHelper.getTilesetsHelper().getTilesetsInProject();
                Tileset tileset;

                // Find tileset and update progress locally
                for (int i = 0; i < tilesets.size(); i++){
                    tileset = tilesets.get(i);
                    if (tileset.getTilesetName().equals(fileNameStr)){
                        tileset.setDownloadProgress(progress[0]);
                    }
                }

                // Tell app that progress has been updated (every 5% for performance)
                if (progress[0] % 5 == 0){
                    onUpdate();
                }

                // Show Started Download (once)
                if (showSysMessage[0]) {
                    // Show sysMessage
                    Toast sysMessage = new Toast(activity);
                    String startedDownloading = activity.getString(R.string.tileset_started_downloading_msg);
                    sysMessage.makeText(activity, startedDownloading + " " + fileNameStr + "!", Toast.LENGTH_SHORT).show();

                    showSysMessage[0] = false;
                }

                // Show finished download (once)
                if (progress[0] > 99.9 && showSysMessage[1]) {

                    // Show sysMessage
                    Toast sysMessage = new Toast(activity);
                    String finishedDownloading = activity.getString(R.string.tileset_finished_downloading_msg);
                    sysMessage.makeText(activity, finishedDownloading + " " + fileNameStr + "!", Toast.LENGTH_SHORT).show();

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
