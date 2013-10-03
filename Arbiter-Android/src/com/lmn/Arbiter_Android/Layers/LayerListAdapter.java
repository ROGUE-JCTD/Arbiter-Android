package com.lmn.Arbiter_Android.Layers;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LayerListAdapter extends ArrayAdapter<LayerListItem> {

	private ArrayList<LayerListItem> items;
	
	public LayerListAdapter(Context context, int resource) {
		super(context, resource);
		// TODO Auto-generated constructor stub
	}
	
	public LayerListAdapter(Context context, int resource, ArrayList<LayerListItem> items){
		super(context, resource, items);
		
		this.items = items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		// Inflate the layout
		if(view == null){
			LayoutInflater inflater = LayoutInflater.from(getContext());
			view = inflater.inflate(R.layout.layer_list_item, null);
		}
		
		LayerListItem listItem = items.get(position);
		
		if(listItem != null){
			TextView layerName = (TextView) view.findViewById(R.id.layerName);
			TextView serverName = (TextView) view.findViewById(R.id.serverName);
			
			if(layerName != null){
				layerName.setText(listItem.getLayerName());
			}
			
			if(serverName != null){
				serverName.setText(listItem.getServerName());
			}
		}
		
		return view;
	}

}
