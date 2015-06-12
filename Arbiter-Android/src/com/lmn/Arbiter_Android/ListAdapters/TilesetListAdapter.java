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
import android.widget.ImageButton;
import android.widget.TextView;

import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesetsHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.GeometryEditor.GeometryEditor;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;
import com.lmn.Arbiter_Android.R;

public class TilesetListAdapter extends BaseAdapter implements ArbiterAdapter<ArrayList<Tileset>>{
	private MapChangeListener mapChangeListener;

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

			try {
				mapChangeListener = (MapChangeListener) activity;
			} catch (ClassCastException e){
				throw new ClassCastException(activity.toString()
						+ " must implement MapChangeListener");
			}
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
				tilesetName.setText(tileset.getName());
			}

			/*if(viewServerOnClickEnabled && !serverName.getText().equals("OpenStreetMap")){
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
					
						// Open the add server dialog
						(new ArbiterDialogs(activity.getApplicationContext(), activity.getResources(),
								activity.getSupportFragmentManager())).showAddServerDialog(server);
					}
				});
			}*/
			
			ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteTileset);
			
			if(deleteButton != null){
				deleteButton.setEnabled(true);
				deleteButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						if(makeSureNotEditing()){
							displayDeletionAlert(tileset);
						}
					}
            		
            	});
            }
		}
		
		return view;
	}
	
	// Return true if not editing
    private boolean makeSureNotEditing(){
    		
		int editMode = mapChangeListener.getMapChangeHelper().getEditMode();
		
		if(editMode == GeometryEditor.Mode.OFF || editMode == GeometryEditor.Mode.SELECT){
			return true;
		}
			
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(R.string.finish_editing_title);
		builder.setMessage(R.string.finish_editing_message);
		builder.setIcon(R.drawable.icon);
		builder.setPositiveButton(android.R.string.ok, null);
		
		builder.create().show();
		
		return false;
    }
    
	private void displayDeletionAlert(final Tileset tileset){
		final Context context = activity.getApplicationContext();
		
		TilesetsHelper.getTilesetsHelper().deletionAlert(activity, new Runnable(){

			@Override
			public void run() {
				final String deletingTilesetTitle = context.getResources().getString(R.string.deleting_tileset);
				final String deletingTilesetMsg = context.getResources().getString(R.string.deleting_tileset_msg);
				
				final ProgressDialog dialog = ProgressDialog.show(activity,
						deletingTilesetTitle, deletingTilesetMsg, true);
				
				CommandExecutor.runProcess(new Runnable(){
					@Override
					public void run() {
						
						TilesetsHelper.getTilesetsHelper().delete(
								activity, tileset);
						
						/*if(mapChangeListener != null){
							mapChangeListener.getMapChangeHelper()
								.onTilesetDeleted(tileset.getId());
						}*/
						
						dialog.dismiss();
					}
					
				});
			}
		}, tileset.getName());
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
				tilesetName.setText(listItem.getName());
			}
		}
		
		return view;
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



