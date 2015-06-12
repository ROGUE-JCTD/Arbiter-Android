package com.lmn.Arbiter_Android.ListAdapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesetsHelper;
import com.lmn.Arbiter_Android.R;

import java.util.ArrayList;
import java.util.HashMap;
import android.util.Log;

public class AddTilesetsListAdapter extends BaseAdapter implements ArbiterAdapter<ArrayList<Tileset>>{
	private ArrayList<Tileset> items;
	private ArrayList<Tileset> checkedTilesets;

	private LayoutInflater inflater;
	private int itemLayout;
	private Context context;

	public AddTilesetsListAdapter(Context context, int itemLayout) {
		this.context = context;
		this.inflater = LayoutInflater.from(this.context);
		this.items = new ArrayList<Tileset>();
		this.checkedTilesets = new ArrayList<Tileset>();
		this.itemLayout = itemLayout;
	}

	public String convertFilesize(double number){
		// Will convert from bytes to KB, MB, or GB
		String result = "Size: ";

		// GB
		if (number > 1073741824.0){
			String num = String.format("%.2f", (number / 1073741824.0));
			result += num + "GB";
		}
		// MB
		else if (number > 1048576.0){
			String num = String.format("%.2f", (number / 1048576.0));
			result += num + "MB";
		}
		// KB
		else{
			String num = String.format("%.2f", (number / 1024.0));
			result += num + "KB";
		}

		return result;
	}
	
	public void setData(ArrayList<Tileset> items){
		this.items = items;
		
		setCheckedTilesets();
		
		notifyDataSetChanged();
	}
	
	private void setCheckedTilesets(){
		if(items != null && !items.isEmpty()
				&& !checkedTilesets.isEmpty()){
			
			// key: server_id:featuretype
			// value: Boolean
			HashMap<String, Integer> tilesetsAlreadyChecked = new HashMap<String, Integer>();
			
			String key = null;
			Tileset currentTileset = null;
			int i;
			
			// Add all of the layers that are checked
			for(i = 0; i < checkedTilesets.size(); i++){
				currentTileset = checkedTilesets.get(i);
				
				key = Tileset.buildTilesetKey(currentTileset);
				
				if(!tilesetsAlreadyChecked.containsKey(key)){
					tilesetsAlreadyChecked.put(key, i);
				}
			}
			
			// If the layer is supposed to be checked, check it
			for(i = 0; i < items.size(); i++){
				currentTileset = items.get(i);
				
				key = Tileset.buildTilesetKey(currentTileset);
				
				if(tilesetsAlreadyChecked.containsKey(key)){
					currentTileset.setChecked(true);
					
					// Replace the Tileset in the checkedTilesets list
					// with the Tileset from the new list, for
					// unchecking to work properly
					replaceCheckedTileset(tilesetsAlreadyChecked, key, currentTileset);
				}
			}
		}
	}
	
	private void replaceCheckedTileset(HashMap<String, Integer> layersAlreadyChecked,
			String key, Tileset tileset){
		
		int replaceAt = layersAlreadyChecked.get(key);
		
		checkedTilesets.set(replaceAt, tileset);
	}
	
	/**
	 * @param position The index of the list item
	 * @param convertView A view that can be reused (For saving memory)
	 * @param parent 
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		if(view == null){
			view = inflater.inflate(itemLayout, null);
		}

		Tileset listItem = items.get(position);
		
		if(listItem != null){
			TextView tilesetName = (TextView) view.findViewById(R.id.tilesetName);
			TextView serverName = (TextView) view.findViewById(R.id.serverName);
			TextView fileSize = (TextView) view.findViewById(R.id.tilesetFilesize);
			CheckBox checkbox = (CheckBox) view.findViewById(R.id.addTilesetCheckbox);
			
			if(tilesetName != null){
				tilesetName.setText(listItem.getName());
			}
			
			if(serverName != null){
				serverName.setText(listItem.getSourceId());
			}

			if(fileSize != null){
				String sizeText = convertFilesize(listItem.getFilesize());
				fileSize.setText(sizeText);
			}
			
			view.setOnClickListener(new OnClickListener(){
				
				@Override
				public void onClick(View v) {
					CheckBox checkbox = (CheckBox) v.findViewById(R.id.addTilesetCheckbox);
					checkbox.performClick();
				}
				
			});
			
			if(checkbox != null){
				checkbox.setChecked(listItem.isChecked());
				
				checkbox.setTag(Integer.valueOf(position));
				
				checkbox.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						Integer position = (Integer) ((CheckBox) v).getTag();
						Tileset listItem = items.get(position);
						boolean checked = !listItem.isChecked();
						
						listItem.setChecked(checked);
						
						if(checked){
							checkedTilesets.add(listItem);
						}else{
							checkedTilesets.remove(listItem);
						}
					}
				});
			}
		}
		
		return view;
	}


	@Override
	public int getCount() {
		if(this.items != null){
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
	
	public ArrayList<Tileset> getCheckedTilesets(){
		return this.checkedTilesets;
	}
}

