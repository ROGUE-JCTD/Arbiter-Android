package com.lmn.Arbiter_Android.OrderLayers;

import java.util.ArrayList;

import android.util.Log;

import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.ListAdapters.OverlayList;

public class OrderLayersModel {
	public static final String TAG = "OrderLayersModel";
	private ArrayList<Layer> layers;
	private ArrayList<Layer> copy;
	private OverlayList adapter;
	
	public OrderLayersModel(OverlayList adapter) throws OrderLayersModelException{
		
		if(adapter == null){
			throw new OrderLayersModelException("Adapter must not be null");
		}
		
		this.layers = null;
		this.copy = null;
		this.adapter = adapter;
	}
	
	public void setLayers(ArrayList<Layer> layers){
		if(layers == null){
			return;
		}
		
		this.layers = layers;
		this.copy = getCopyOfLayers(layers);
	}
	
	private ArrayList<Layer> getCopyOfLayers(ArrayList<Layer> layers){
		
		int count = layers.size();
		
		ArrayList<Layer> _copy = new ArrayList<Layer>(count);
		
		for(int i = 0; i < count; i++){
			_copy.add(new Layer(layers.get(i)));
		}
		
		return _copy;
	}
	
	public void moveLayerDown(int layerIndex){
		Log.w("OrderLayersModel", "OrderLayersModel move layer down: " + layerIndex);
		
		swapLayerWithLayerBelow(layerIndex);
		
		this.adapter.onDataUpdated();
	}
	
	public void moveLayerUp(int layerIndex){
		Log.w("OrderLayersModel", "OrderLayersModel move layer up: " + layerIndex);
		swapLayerWithLayerAbove(layerIndex);
		
		this.adapter.onDataUpdated();
	}
	
	public ArrayList<Layer> getBackup(){
		return this.copy;
	}
	
	private void swapLayerWithLayerBelow(int layerIndex){
		
		swapLayers(layerIndex, layerIndex + 1);
	}
	
	private void swapLayerWithLayerAbove(int layerIndex){
		
		swapLayers(layerIndex, layerIndex - 1);
	}
	
	private boolean indexIsWithinBounds(int index){
		return (layers != null) && (index >= 0) && (index < layers.size());
	}
	
	private void swapLayers(int layerIndex1, int layerIndex2){
		
		if(!indexIsWithinBounds(layerIndex1) || !indexIsWithinBounds(layerIndex2)){
			return;
		}
		
		Layer layer1 = layers.get(layerIndex1);
		
		Layer layer2 = layers.get(layerIndex2);
		
		if(layer1 == null || layer2 == null){
			return;
		}
		
		swapLayerOrders(layer1, layer2);
		
		layers.set(layerIndex1, layer2);
		layers.set(layerIndex2, layer1);
	}
	
	private void swapLayerOrders(Layer layer1, Layer layer2){
		
		int layerOrder1 = layer1.getLayerOrder();
		
		layer1.setLayerOrder(layer2.getLayerOrder());
		
		layer2.setLayerOrder(layerOrder1);
	}
}
