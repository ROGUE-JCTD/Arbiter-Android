package com.lmn.Arbiter_Android.Comparators;

import java.util.Comparator;

import com.lmn.Arbiter_Android.BaseClasses.Layer;

public class CompareAddLayersListItems implements Comparator<Layer>{

	@Override
	public int compare(Layer item1, Layer item2) {
		
		return item1.getLayerTitle().compareToIgnoreCase(item2.getLayerTitle());
	}
}
