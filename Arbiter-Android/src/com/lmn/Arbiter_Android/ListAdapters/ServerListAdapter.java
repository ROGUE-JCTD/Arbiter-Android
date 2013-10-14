package com.lmn.Arbiter_Android.ListAdapters;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ListItems.ServerListItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ServerListAdapter extends ArrayAdapter<ServerListItem> {

	private ArrayList<ServerListItem> items;
	
	public ServerListAdapter(Context context, int resource) {
		super(context, resource);
		// TODO Auto-generated constructor stub
	}
	
	public ServerListAdapter(Context context, int resource, ArrayList<ServerListItem> items){
		super(context, resource, items);
		
		this.items = items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		// Inflate the layout
		if(view == null){
			LayoutInflater inflater = LayoutInflater.from(getContext());
			view = inflater.inflate(R.layout.server_list_item, null);
		}
		
		ServerListItem listItem = items.get(position);
		
		if(listItem != null){
			TextView serverName = (TextView) view.findViewById(R.id.serverName);
			
			if(serverName != null){
				serverName.setText(listItem.getServerName());
			}
		}
		
		return view;
	}

}
