package com.lmn.Arbiter_Android.ListViews;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ListAdapters.AddLayersListAdapter;
import com.lmn.Arbiter_Android.ListItems.AddLayersListItem;

import android.content.Context;
import android.util.AttributeSet;

public class AddLayersListView extends CustomListView<AddLayersListItem> {
	private int layout;
	
	public AddLayersListView(Context context) {
		super(context);
		layout = R.layout.add_layers_list_item;
	}
	
	public AddLayersListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		layout = R.layout.add_layers_list_item;
	}
	
	public AddLayersListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		layout = R.layout.add_layers_list_item;
	}
	
	@Override
	public void setListAdapter(){
		setAdapter(new AddLayersListAdapter(getContext(), layout, getList()));
	}
	
	/**
	 * Returns the ListView to populate the ListView
	 */
	@Override
	public ArrayList<AddLayersListItem> getList() {
		ArrayList<AddLayersListItem> list = new ArrayList<AddLayersListItem>();
		
		list.add(new AddLayersListItem("Layer1", "Server1"));
		list.add(new AddLayersListItem("Layer2", "Server1"));
		list.add(new AddLayersListItem("Layer3", "Server1"));
		list.add(new AddLayersListItem("Layer4", "Server1"));
		list.add(new AddLayersListItem("Layer5", "Server1"));
		list.add(new AddLayersListItem("Layer6", "Server1"));
		list.add(new AddLayersListItem("Layer7", "Server1"));
		list.add(new AddLayersListItem("Layer8", "Server1"));
		list.add(new AddLayersListItem("Layer9", "Server1"));
		list.add(new AddLayersListItem("Layer10", "Server1"));
		list.add(new AddLayersListItem("Layer11", "Server1"));
		list.add(new AddLayersListItem("Layer12", "Server1"));
		list.add(new AddLayersListItem("Layer13", "Server1"));
		list.add(new AddLayersListItem("Layer14", "Server1"));
		list.add(new AddLayersListItem("Layer15", "Server1"));
		list.add(new AddLayersListItem("Layer16", "Server1"));
		list.add(new AddLayersListItem("Layer17", "Server1"));
		list.add(new AddLayersListItem("Layer18", "Server1"));
		
		return list;
	}

}
