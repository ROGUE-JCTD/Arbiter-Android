package com.lmn.Arbiter_Android.ListAdapters;

import java.util.ArrayList;
import java.util.HashMap;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Layer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class AddLayersListAdapter extends BaseAdapter implements ArbiterAdapter<ArrayList<Layer>>{
	private ArrayList<Layer> items;
	private ArrayList<Layer> checkedLayers;
	
	private LayoutInflater inflater;
	private int itemLayout;
	private Context context;
	
	public AddLayersListAdapter(Context context, int itemLayout) {
		this.context = context;
		this.inflater = LayoutInflater.from(this.context);
		this.items = new ArrayList<Layer>();
		this.checkedLayers = new ArrayList<Layer>();
		this.itemLayout = itemLayout;
	}
	
	public void setData(ArrayList<Layer> items){
		this.items = items;
		
		setCheckedLayers();
		
		notifyDataSetChanged();
	}
	
	private void setCheckedLayers(){
		if(items != null && !items.isEmpty()
				&& !checkedLayers.isEmpty()){
			
			// key: server_id:featuretype
			// value: Boolean
			HashMap<String, Integer> layersAlreadyChecked = new HashMap<String, Integer>();
			
			String key = null;
			Layer currentLayer = null;
			int i;
			
			// Add all of the layers that are checked
			for(i = 0; i < checkedLayers.size(); i++){
				currentLayer = checkedLayers.get(i);
				
				key = Layer.buildLayerKey(currentLayer);
				
				if(!layersAlreadyChecked.containsKey(key)){
					layersAlreadyChecked.put(key, i);
				}
			}
			
			// If the layer is supposed to be checked, check it
			for(i = 0; i < items.size(); i++){
				currentLayer = items.get(i);
				
				key = Layer.buildLayerKey(currentLayer);
				
				if(layersAlreadyChecked.containsKey(key)){
					currentLayer.setChecked(true);
					
					// Replace the Layer in the checkedLayers list
					// with the Layer from the new list, for
					// unchecking to work properly
					replaceCheckedLayer(layersAlreadyChecked, key, currentLayer);
				}
			}
		}
	}
	
	private void replaceCheckedLayer(HashMap<String, Integer> layersAlreadyChecked, 
			String key, Layer layer){
		
		int replaceAt = layersAlreadyChecked.get(key);
		
		checkedLayers.set(replaceAt, layer);
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
		
		Layer listItem = items.get(position);
		
		if(listItem != null){
			TextView layerName = (TextView) view.findViewById(R.id.layerName);
			TextView serverName = (TextView) view.findViewById(R.id.serverName);
			CheckBox checkbox = (CheckBox) view.findViewById(R.id.addLayerCheckbox);
			
			if(layerName != null){
				layerName.setText(listItem.getLayerTitle());
			}
			
			if(serverName != null){
				serverName.setText(listItem.getServerName());
			}
			
			view.setOnClickListener(new OnClickListener(){
				
				@Override
				public void onClick(View v) {
					CheckBox checkbox = (CheckBox) v.findViewById(R.id.addLayerCheckbox);
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
						Layer listItem = items.get(position);
						boolean checked = !listItem.isChecked();
						
						listItem.setChecked(checked);
						
						if(checked){
							checkedLayers.add(listItem);
						}else{
							checkedLayers.remove(listItem);
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
	public Layer getItem(int position) {
		return this.items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public ArrayList<Layer> getCheckedLayers(){
		return this.checkedLayers;
	}
}