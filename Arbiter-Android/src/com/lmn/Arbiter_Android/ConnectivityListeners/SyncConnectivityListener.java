package com.lmn.Arbiter_Android.ConnectivityListeners;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesetsHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;
import com.lmn.Arbiter_Android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.ImageButton;
import android.content.Context;

public class SyncConnectivityListener extends ConnectivityListener {

    private boolean showDialog[]; // control how many times a dialog is shown
    private Activity activity;
    private ImageButton syncButton;
    private int green;
    private int red;

    public SyncConnectivityListener(Activity activity, ImageButton syncButton) {
        super(activity);

        this.showDialog = new boolean[2];
        this.showDialog[0] = true;
        this.showDialog[1] = true;

        this.activity = activity;
        this.syncButton = syncButton;
        this.green = activity.getApplicationContext().getResources().getColor(R.color.transparent_green);
        this.red = activity.getApplicationContext().getResources().getColor(R.color.transparent_red);
    }

    @Override
    public void onConnectivityChanged(boolean isConnected) {

        if (isConnected) {
            onConnected();
        } else {
            onDisconnected();
        }
    }

    private void onConnected() {
        syncButton.setBackgroundColor(green);
        syncButton.setEnabled(true);

        //showDialog[0] = true; // only remind once - No Tilesets with Project
        showDialog[1] = true; // remind every time - No Tilesets no Project
    }

    private void onDisconnected() {
        syncButton.setBackgroundColor(red);
        syncButton.setEnabled(true);

        // If there is currently a project when disconnected (-1 accounts for default project)
        if (ProjectStructure.getProjectStructure().getProjects().length - 1 > 0) {
            if (showDialog[0]) {
                // Let the user know they should be aware of offline maps
                TilesetsHelper.getTilesetsHelper().noConnectionTilesetDialog(activity);
                showDialog[0] = false;
            }
        } else {
            if (showDialog[1]) {
                Context context = activity;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle(R.string.no_network);

                String errorMsg = context.getString(R.string.disconnected_no_projects);
                builder.setMessage(errorMsg);

                builder.setIcon(context.getResources().getDrawable(R.drawable.icon));
                builder.setPositiveButton(android.R.string.ok, null);

                builder.create().show();
                showDialog[1] = false;
            }
        }
    }
}
