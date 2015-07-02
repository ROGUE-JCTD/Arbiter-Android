package com.lmn.Arbiter_Android.Comparators;

import com.lmn.Arbiter_Android.BaseClasses.Tileset;

import java.util.Comparator;

public class CompareAddTilesetsListItems implements Comparator<Tileset>{

	@Override
	public int compare(Tileset item1, Tileset item2) {
		
		return item1.getTilesetName().compareToIgnoreCase(item2.getTilesetName());
	}
}
