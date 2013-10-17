package com.lmn.Arbiter_Android.ListAdapters;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ListItems.ServerListItem;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ServerSpinnerAdapter extends BaseAdapter{

	private ServerListItem[] items;
	private final LayoutInflater inflater;
	
	public ServerSpinnerAdapter(Context context){
			inflater = LayoutInflater.from(context);
			items = new ServerListItem[0];
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
			view = inflater.inflate(android.R.layout.simple_dropdown_item_1line, null);
		}
		
		ServerListItem listItem = items[position];
		
		if(listItem != null){
			TextView serverName = (TextView) view.findViewById(android.R.id.text1);
			
			if(serverName != null){
				serverName.setText(listItem.getServerName());
				serverName.setTextColor(Color.BLACK);
			}
		}
		
		return view;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		if(view == null){
			view = inflater.inflate(R.layout.spinner_item, null);
		}
		
		ServerListItem listItem = items[position];
	
		if(listItem != null){
			TextView serverName = (TextView) view.findViewById(R.id.spinnerText);
		
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
	public Object getItem(int position) {
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
