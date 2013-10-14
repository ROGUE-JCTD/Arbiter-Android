package com.lmn.Arbiter_Android.ListViews;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ListAdapters.ServerListAdapter;
import com.lmn.Arbiter_Android.ListItems.ServerListItem;

import android.content.Context;
import android.util.AttributeSet;

public class ServersListView extends CustomListView<ServerListItem> {
	private int layout;
	
	public ServersListView(Context context) {
		super(context);
		layout = R.layout.server_list_item;
	}
	
	public ServersListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		layout = R.layout.server_list_item;
	}
	
	public ServersListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		layout = R.layout.server_list_item;
	}
	
	@Override
	public void setListAdapter(){
		setAdapter(new ServerListAdapter(getContext(), layout, getList()));
	}
	
	/**
	 * Returns the ListView to populate the ListView
	 */
	@Override
	public ArrayList<ServerListItem> getList() {
		ArrayList<ServerListItem> list = new ArrayList<ServerListItem>();
		
		list.add(new ServerListItem("Server1"));
		list.add(new ServerListItem("Server2"));
		list.add(new ServerListItem("Server3"));
		list.add(new ServerListItem("Server4"));
		list.add(new ServerListItem("Server5"));
		list.add(new ServerListItem("Server6"));
		list.add(new ServerListItem("Server7"));
		list.add(new ServerListItem("Server8"));
		list.add(new ServerListItem("Server9"));
		list.add(new ServerListItem("Server10"));
		list.add(new ServerListItem("Server11"));
		list.add(new ServerListItem("Server12"));
		list.add(new ServerListItem("Server13"));
		list.add(new ServerListItem("Server14"));
		list.add(new ServerListItem("Server15"));
		list.add(new ServerListItem("Server16"));
		list.add(new ServerListItem("Server17"));
		list.add(new ServerListItem("Server18"));
		
		return list;
	}

}
