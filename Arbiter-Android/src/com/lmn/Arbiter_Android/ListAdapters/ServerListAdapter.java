package com.lmn.Arbiter_Android.ListAdapters;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ListItems.ServerListItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ServerListAdapter extends BaseAdapter{

	private ServerListItem[] items;
	private final LayoutInflater inflater;
	private int itemLayout;
	private int textId;
	private int dropDownLayout;
	
	public ServerListAdapter(Context context, int itemLayout, 
			int textId){
		
			inflater = LayoutInflater.from(context);
			items = new ServerListItem[0];
			this.itemLayout = itemLayout;
			this.textId = textId;
			this.dropDownLayout = R.layout.drop_down_item;
	}
	
	public ServerListAdapter(Context context, int itemLayout, 
			int textId, Integer dropDownLayout){
		
			inflater = LayoutInflater.from(context);
			items = new ServerListItem[0];
			this.itemLayout = itemLayout;
			this.textId = textId;
			this.dropDownLayout = dropDownLayout;
	}
	
	public void setData(ServerListItem[] data){
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
		
		ServerListItem listItem = items[position];
		
		if(listItem != null){
			TextView serverName = (TextView) view.findViewById(textId);
			
			if(serverName != null){
				serverName.setText(listItem.getServerName());
			}
		}
		
		return view;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		if(view == null){
			view = inflater.inflate(dropDownLayout, null);
		}
		
		ServerListItem listItem = items[position];
	
		if(listItem != null){
			TextView serverName = (TextView) view.findViewById(textId);
		
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
	public ServerListItem getItem(int position) {
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
