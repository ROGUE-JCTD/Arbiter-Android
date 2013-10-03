package com.lmn.Arbiter_Android.Layers;

import com.lmn.Arbiter_Android.ArbiterList;

public class LayerList extends ArbiterList<LayerListItem>{

	public LayerList(){
		super();
		populateLayerList();
	}

	/**
	 * Populate the layer list
	 */
	public void populateLayerList(){
		this.list.add(new LayerListItem("Layer1", "Server1"));
		this.list.add(new LayerListItem("Layer2", "Server2"));
	}
	
	@Override
	public void onAddItem(LayerListItem item) {
		
	}

	@Override
	public void onRemoveItem(LayerListItem item) {
		
	}
}
