package com.lmn.Arbiter_Android.ListAdapters;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.FileDownloader.FileDownloader;
import com.lmn.Arbiter_Android.DatabaseHelpers.FileDownloader.DownloadListener;
import com.lmn.Arbiter_Android.DatabaseHelpers.MBTilesDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesetsHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;
import com.lmn.Arbiter_Android.R;

import java.util.ArrayList;


public class AddTilesetsListAdapter extends BaseAdapter implements ArbiterAdapter<ArrayList<Tileset>> {
    private ArrayList<Tileset> items;

    private LayoutInflater inflater;
    private int itemLayout;
    private FragmentActivity activity;


    public AddTilesetsListAdapter(FragmentActivity activity, int itemLayout) {
        this.activity = activity;
        this.inflater = LayoutInflater.from(this.activity.getApplicationContext());
        this.items = new ArrayList<Tileset>();
        this.itemLayout = itemLayout;
    }

    public void setData(ArrayList<Tileset> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    /**
     * @param position    The index of the list item
     * @param convertView A view that can be reused (For saving memory)
     * @param parent
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = inflater.inflate(itemLayout, null);
        }

        final Tileset listItem = items.get(position);

        if (items.size() > 0) {
            if (listItem != null) {
                TextView tilesetName = (TextView) view.findViewById(R.id.tilesetName);
                TextView serverName = (TextView) view.findViewById(R.id.serverName);
                TextView fileSize = (TextView) view.findViewById(R.id.tilesetFilesize);
                final ImageButton downloadButton = (ImageButton) view.findViewById(R.id.download_tileset_button);

                if (tilesetName != null) {
                    tilesetName.setText(listItem.getTilesetName());
                }

                if (serverName != null) {
                    serverName.setText(listItem.getLayerName());
                }

                if (fileSize != null) {
                    String sizeText = TilesetsHelper.getTilesetsHelper().convertFilesize(listItem.getFilesize());
                    fileSize.setText(sizeText);
                }

                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        (new ArbiterDialogs(activity.getApplicationContext(),
                                activity.getResources(),
                                activity.getSupportFragmentManager())).showTilesetInfoDialog(listItem);
                    }
                });


                if (downloadButton != null) {

                    // Should we replace/hide the button?

                    // Check for In Database
                    boolean isInDatabase = false;
                    boolean updatable = false;
                    boolean isDownloading = false;
                    ArrayList<Tileset> tilesetsInProject = getTilesetsInProject();
                    for (int i = 0; i < tilesetsInProject.size(); i++) {
                        Tileset tileset = tilesetsInProject.get(i);

                        if (tileset.getTilesetName().equals(listItem.getTilesetName())) {
                            // Found in database
                            isInDatabase = true;

                            // Check if it is currently downloading
                            if (tileset.getIsDownloading()) {
                                isInDatabase = false;
                                isDownloading = true;
                                break;
                            }

                            // See if it's updatable
                            if (!tileset.getCreatedTime().equals(listItem.getCreatedTime())) {
                                // need to check for more recent
                                updatable = true;
                            }

                            break;
                        }
                    }

                    if (isInDatabase) {

                        if (updatable) {
                            // RED

                            // Put Refresh (Update)
                            //downloadButton.setImageResource();
                            downloadButton.setColorFilter(0xFFFF0000); // debug
                            setButtonForDownload(downloadButton, listItem);
                        } else {
                            // GREEN

                            // Put Check Mark
                            //downloadButton.setImageResource();
                            downloadButton.setColorFilter(0xFF00FF00); // debug
                            downloadButton.setOnClickListener(null);
                        }
                    } else if (isDownloading) {
                        // BLUE

                        // Put Spinner
                        //downloadButton.setImageResource();
                        //setButtonIsDownloading(downloadButton, listItem);
                        downloadButton.setColorFilter(0xFF0000FF); // debug
                    } else {
                        // Set Downloadable
                        //downloadButton.setImageResource();
                        downloadButton.setColorFilter(0xFFFFFFFF); // debug
                        setButtonForDownload(downloadButton, listItem);
                    }
                }
            }
        }

        return view;
    }

    private void setButtonForDownload(final ImageButton downloadButton, final Tileset listItem) {

        final Context context = activity.getApplicationContext();
        final TilesetsHelper tilesetHelper = TilesetsHelper.getTilesetsHelper();

        downloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                if (listItem.getFilesize() > 0) {

                    // Bring up download dialog (activity, function to download checked tiles, final filesize, tilesetName)
                    tilesetHelper.downloadSizeDialog(activity, new Runnable() {

                        @Override
                        public void run() {

                            // Set started downloading
                            downloadButton.setOnClickListener(null);
                            downloadButton.setColorFilter(0xFF0000FF); // debug
                            listItem.setIsDownloading(true);
                            listItem.setDownloadProgress(0);

                            CommandExecutor.runProcess(new Runnable() {
                                @Override
                                public void run() {

                                    String URL = listItem.getDownloadURL();
                                    String output = ProjectStructure.getTileSetsRoot();
                                    startDownloadingTileset(URL, output, listItem, downloadButton);

                                    String file = listItem.getTilesetName() + tilesetHelper.getTilesetDownloadExtension();
                                    listItem.setFileLocation("file://TileSets/" + file);

                                    // Put JSON into Database BS (to keep track of it)
                                    insertTilesetIntoDB(context, tilesetHelper, listItem);
                                }
                            });

                            // Check if needed
                            notifyDataSetChanged();

                        }
                    }, listItem.getFilesize(), listItem.getTilesetName());

                } else {
                    tilesetHelper.badFileDialog(activity, listItem.getTilesetName());
                }
            }
        });

    }

    private void startDownloadingTileset(String URL, String output, final Tileset tileset, final ImageButton downloadButton) {

        // Start Downloading Files
        new FileDownloader(URL, output, activity, tileset,
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
                        downloadButton.setOnClickListener(null);
                        downloadButton.setColorFilter(0xFF00FF00);

                        // Update tileset / add to local ArrayList
                        Context context = activity.getApplicationContext();
                        updateTilesetInDB(context, tileset);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        // In case of Error
                        removeTilesetFromDB(tileset);
                        //notifyDataSetChanged();
                    }
                });
    }

    private void insertTilesetIntoDB(Context context, TilesetsHelper helper, Tileset tilesetToAdd) {
        ApplicationDatabaseHelper appHelper = ApplicationDatabaseHelper.getHelper(context);

        // Put Information into DB
        helper.insert(appHelper.getWritableDatabase(), context, tilesetToAdd);
    }

    private void updateTilesetInDB(Context context, Tileset tileset) {
        ApplicationDatabaseHelper appHelper = ApplicationDatabaseHelper.getHelper(context);

        TilesetsHelper.getTilesetsHelper().update(appHelper.getWritableDatabase(), context, tileset);
    }

    private void removeTilesetFromDB(Tileset tileset) {
        TilesetsHelper.getTilesetsHelper().delete(activity, tileset);
    }

    @Override
    public int getCount() {
        if (this.items != null) {
            return this.items.size();
        }

        return 0;
    }

    @Override
    public Tileset getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ArrayList<Tileset> getTilesetsInProject() {
        return TilesetsHelper.getTilesetsHelper().getTilesetsInProject();
    }
}

