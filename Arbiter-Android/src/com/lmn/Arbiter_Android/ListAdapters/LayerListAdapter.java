package com.lmn.Arbiter_Android.ListAdapters;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ListItems.Layer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LayerListAdapter extends BaseAdapter{

	private Layer[] items;
	private final LayoutInflater inflater;
	private int itemLayout;
	
	public LayerListAdapter(Context context, int itemLayout){
		
			inflater = LayoutInflater.from(context);
			items = new Layer[0];
			this.itemLayout = itemLayout;
	}
	public void setData(Layer[] data){
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
		
		Layer listItem = items[position];
		
		if(listItem != null){
            TextView layerName = (TextView) view.findViewById(R.id.layerName);
            TextView serverName = (TextView) view.findViewById(R.id.serverName);
            
            if(layerName != null){
                    layerName.setText(listItem.getLayerTitle());
            }
            
            if(serverName != null){
                    serverName.setText(listItem.getServerName());
            }
		}
		
		return view;
	}
	
	@Override
	public int getCount() {
		if(items == null){
			return 0;
		}
		
		return items.length;
	}

	@Override
	public Layer getItem(int position) {
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
