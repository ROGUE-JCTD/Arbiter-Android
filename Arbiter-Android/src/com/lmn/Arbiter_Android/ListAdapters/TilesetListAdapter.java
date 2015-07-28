package com.lmn.Arbiter_Android.ListAdapters;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesetsHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.FileDownloader.DownloadListener;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.GeometryEditor.GeometryEditor;
import com.lmn.Arbiter_Android.R;


import android.util.Log;

public class TilesetListAdapter extends BaseAdapter implements ArbiterAdapter<ArrayList<Tileset>>{

	private ArrayList<Tileset> items;
	private final LayoutInflater inflater;
	private int itemLayout;
	private int textId;
	private int dropDownLayout;
	private final FragmentActivity activity;
	private boolean viewServerOnClickEnabled;

	public TilesetListAdapter(FragmentActivity activity, int itemLayout,
							  int textId){

			inflater = LayoutInflater.from(activity.getApplicationContext());
			items = new ArrayList<Tileset>();
			this.itemLayout = itemLayout;
			this.textId = textId;
			this.dropDownLayout = R.layout.drop_down_item;
			this.activity = activity;
			this.viewServerOnClickEnabled = true;
	}

	public TilesetListAdapter(FragmentActivity activity, int itemLayout,
							  int textId, Integer dropDownLayout){
		
			inflater = LayoutInflater.from(activity.getApplicationContext());
			items = new ArrayList<Tileset>();
			this.itemLayout = itemLayout;
			this.textId = textId;
			this.dropDownLayout = dropDownLayout;
			this.activity = activity;
			this.viewServerOnClickEnabled = true;
	}

	public void Init(){
		DownloadListener.getListener().addToListenerList(updateView());
	}

	public void setData(ArrayList<Tileset> data){
		items = data;
		
		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		// Inflate the layout
		if(view == null){
			view = inflater.inflate(itemLayout, null);
		}
		
		final Tileset tileset = getItem(position);
		
		if(tileset != null){
			TextView tilesetName = (TextView) view.findViewById(textId);
			
			if(tilesetName != null){
				tilesetName.setText(tileset.getTilesetName());
			}

			// When clicking on Tileset, show info
			view.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					(new ArbiterDialogs(activity.getApplicationContext(),
							activity.getResources(),
							activity.getSupportFragmentManager())).showTilesetInfoDialog(tileset);
				}
			});

			ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteTileset);
			if(deleteButton != null){

				ProgressBar downloadBar = (ProgressBar)view.findViewById(R.id.downloading_tileset_bar);

				// Change button if downloading
				if (tileset.getIsDownloading()){

					// Work around from ProgressBar bug (set progress to 0 then back to current to invalidate)
					downloadBar.setProgress(0);
					downloadBar.setProgress(tileset.getDownloadProgress());

					// Make visible/refresh
					downloadBar.setAlpha(1.0f);
					downloadBar.invalidate();

					// Debug
					deleteButton.setColorFilter(0xFFFF0000);

					// Allow user to Stop Download
					deleteButton.setEnabled(true);
					deleteButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
								// Cancel download dialog
								displayCancellationAlert(tileset);
						}
					});
				} else {
					//downloadBar.setProgress(0);
					downloadBar.setAlpha(0.0f);

					// Debug
					deleteButton.setColorFilter(0xFFFFFFFF);

					// Allow user to Delete Tileset
					deleteButton.setEnabled(true);
					deleteButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
								displayDeletionAlert(tileset);
						}
					});
				}
            }
		}
		
		return view;
	}

	private void displayDeletionAlert(final Tileset tileset){
		final Context context = activity.getApplicationContext();
		
		TilesetsHelper.getTilesetsHelper().deletionAlert(activity, new Runnable() {

			@Override
			public void run() {
				final String deletingTilesetTitle = context.getResources().getString(R.string.deleting_tileset);
				final String deletingTilesetMsg = context.getResources().getString(R.string.deleting_tileset_msg);

				final ProgressDialog dialog = ProgressDialog.show(activity,
						deletingTilesetTitle, deletingTilesetMsg, true);

				CommandExecutor.runProcess(new Runnable() {
					@Override
					public void run() {

						TilesetsHelper.getTilesetsHelper().delete(
								activity, tileset);

						dialog.dismiss();
					}

				});
			}
		}, tileset.getTilesetName());
	}

	private void displayCancellationAlert(final Tileset tileset){
		final Context context = activity.getApplicationContext();

		TilesetsHelper.getTilesetsHelper().cancellationAlert(activity, new Runnable() {

			@Override
			public void run() {
				final String cancelDownloadTitle = context.getResources().getString(R.string.tileset_cancel_download_title);
				final String cancelDownloadMsg = context.getResources().getString(R.string.tileset_cancel_download_msg);

				final ProgressDialog dialog = ProgressDialog.show(activity,
						cancelDownloadTitle, cancelDownloadMsg, true);

				CommandExecutor.runProcess(new Runnable() {
					@Override
					public void run() {

						TilesetsHelper.getTilesetsHelper().delete(
								activity, tileset);

						dialog.dismiss();
					}

				});
			}
		}, tileset.getTilesetName());
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		if(view == null){
			view = inflater.inflate(dropDownLayout, null);
		}
		
		Tileset listItem = getItem(position);
	
		if(listItem != null){
			TextView tilesetName = (TextView) view.findViewById(textId);
		
			if(tilesetName != null){
				tilesetName.setText(listItem.getTilesetName());
			}
		}
		
		return view;
	}

	public Runnable updateView(){

		// This returns a Runnable to be executed later

		return new Runnable() {
			@Override
			public void run() {

				ArrayList<Tileset> tilesets = TilesetsHelper.getTilesetsHelper().getTilesetsInProject();

				// Find Tileset in ListAdapter's items, update with correct information
				Tileset Item, InProject;
				for (int i = 0; i < items.size(); i++){
					Item = getItem(i);

					// Only check if downloading
					if (Item.getIsDownloading()) {
						for (int j = 0; j < tilesets.size(); j++) {
							InProject = tilesets.get(j);
							if (Item.getTilesetName().equals(InProject.getTilesetName())) {
								Item.setDownloadProgress(InProject.getDownloadProgress());
								notifyDataSetInvalidated();
							}
						}
					}
				}
			}
		};
	}


	
	@Override
	public int getCount() {
		if(items == null){
			return 0;
		}
		
		return items.size();
	}

	@Override
	public Tileset getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}



