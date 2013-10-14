package com.lmn.Arbiter_Android.ListViews;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;


public abstract class CustomListView<ListItemType> extends ListView {

	public CustomListView(Context context) {
		super(context);
	}
	
	public CustomListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setListAdapter();
	}
	
	public CustomListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public abstract void setListAdapter();
	
	/**
	 * Returns the list to populate the ListView with
	 * @return The list to populate the ListView with
	 */
	public abstract List<ListItemType> getList();
}
