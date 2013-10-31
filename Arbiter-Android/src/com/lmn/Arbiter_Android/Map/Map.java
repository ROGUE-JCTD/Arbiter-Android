package com.lmn.Arbiter_Android.Map;

import java.util.ArrayList;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lmn.Arbiter_Android.BaseClasses.Layer;

public class Map{
	private Map(){}
	
	private static Map map = null;
	
	public interface MapChangeListener {
		public void onLayerDeleted(long layerId);
		
		public void onLayerVisibilityChanged(long layerId);
		
		public void onLayersAdded(ArrayList<Layer> layers, long[] layerIds, boolean includeDefaultLayer);
	}
	
	public static Map getMap(){
		if(map == null){
			map = new Map();
		}
		
		return map;
	}
	
	public void addLayers(CordovaWebView webview, final ArrayList<Layer> layers, long[] layerIds, boolean includeDefaultLayer){
		try {
			webview.loadUrl("javascript:app.addLayers(" 
					+ getLayersJSON(layers, layerIds) + ", " + Boolean.toString(includeDefaultLayer)  + ")");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void loadMap(CordovaWebView webview, final ArrayList<Layer> layers, boolean includeDefaultLayer){
		try {
			webview.loadUrl("javascript:app.loadMap(" 
					+ getLayersJSON(layers, null) + ", " + Boolean.toString(includeDefaultLayer)  + ")");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private JSONArray getLayersJSON(ArrayList<Layer> layers, long[] layerIds) throws JSONException{
		if(layers == null){
			return null;
		}
		
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonLayer;
		Layer layer;
		
		for(int i = 0; i < layers.size(); i++){
			layer = layers.get(i);
			jsonLayer = new JSONObject();
			
			if(layerIds == null){
				jsonLayer.put("layerId", layer.getLayerId());
			}else{
				jsonLayer.put("layerId", layerIds[i]);
			}
			
			jsonLayer.put("featureType", layer.getFeatureType());
			jsonLayer.put("srs", layer.getLayerSRS());
			jsonLayer.put("serverUrl", layer.getServerUrl());
			
			jsonArray.put(jsonLayer);
		}
		
		return jsonArray;
	}
	
	public void deleteLayer(CordovaWebView webview, long layerId){
		if(layerId == Layer.DEFAULT_FLAG){
			webview.loadUrl("javascript:app.removeDefaultLayer()");
		}else{
			webview.loadUrl("javascript:app.removeLayer(" 
					+ Long.toString(layerId) + ")");
		}	
	}
}
