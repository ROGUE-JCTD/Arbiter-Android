package com.lmn.Arbiter_Android.Comparators;

import java.util.Comparator;

import com.lmn.Arbiter_Android.ListItems.AddLayersListItem;

public class CompareAddLayersListItems implements Comparator<AddLayersListItem>{

	@Override
	public int compare(AddLayersListItem item1, AddLayersListItem item2) {
		
		return item1.getLayerName().compareToIgnoreCase(item2.getLayerName());
	}
}
