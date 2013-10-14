package com.lmn.Arbiter_Android.ListViews;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ListAdapters.LayerListAdapter;
import com.lmn.Arbiter_Android.ListItems.LayerListItem;

import android.content.Context;
import android.util.AttributeSet;

public class LayersListView extends CustomListView<LayerListItem> {
	private int layout;
	
	public LayersListView(Context context) {
		super(context);
		layout = R.layout.layer_list_item;
	}
	
	public LayersListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		layout = R.layout.layer_list_item;
	}
	
	public LayersListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		layout = R.layout.layer_list_item;
	}
	
	@Override
	public void setListAdapter(){
		setAdapter(new LayerListAdapter(getContext(), layout, getList()));
	}
	
	/**
	 * Returns the ListView to populate the ListView
	 */
	@Override
	public ArrayList<LayerListItem> getList() {
		ArrayList<LayerListItem> list = new ArrayList<LayerListItem>();
		
		list.add(new LayerListItem("Layer1", "Server1"));
		list.add(new LayerListItem("Layer2", "Server1"));
		list.add(new LayerListItem("Layer3", "Server1"));
		list.add(new LayerListItem("Layer4", "Server1"));
		list.add(new LayerListItem("Layer5", "Server1"));
		list.add(new LayerListItem("Layer6", "Server1"));
		list.add(new LayerListItem("Layer7", "Server1"));
		list.add(new LayerListItem("Layer8", "Server1"));
		list.add(new LayerListItem("Layer9", "Server1"));
		list.add(new LayerListItem("Layer10", "Server1"));
		list.add(new LayerListItem("Layer11", "Server1"));
		list.add(new LayerListItem("Layer12", "Server1"));
		list.add(new LayerListItem("Layer13", "Server1"));
		list.add(new LayerListItem("Layer14", "Server1"));
		list.add(new LayerListItem("Layer15", "Server1"));
		list.add(new LayerListItem("Layer16", "Server1"));
		list.add(new LayerListItem("Layer17", "Server1"));
		list.add(new LayerListItem("Layer18", "Server1"));
		
		return list;
	}

}
