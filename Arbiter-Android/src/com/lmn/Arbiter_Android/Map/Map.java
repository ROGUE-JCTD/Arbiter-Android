package com.lmn.Arbiter_Android.Map;

import java.util.ArrayList;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.lmn.Arbiter_Android.BaseClasses.Layer;

public class Map{
	private Map(){}
	
	private static Map map = null;
	
	public interface MapChangeListener {
		public void onLayerDeleted(long layerId);
		
		public void onLayerVisibilityChanged(long layerId);
		
		public void onLayersAdded(ArrayList<Layer> layers, long[] layerIds, 
				boolean includeDefaultLayer, boolean defaultLayerVisibility);
	}
	
	public interface CordovaMap {
		public CordovaWebView getWebView();
	}
	
	public static Map getMap(){
		if(map == null){
			map = new Map();
		}
		
		return map;
	}
	
	public void addLayers(CordovaWebView webview, final ArrayList<Layer> layers, long[] layerIds, 
			boolean includeDefaultLayer, boolean defaultLayerVisibility){
		try {
			//webview.loadUrl("javascript:app.addLayers(" 
			//		+ getLayersJSON(layers, layerIds) + ", " + Boolean.toString(includeDefaultLayer)  + ")");
			
			webview.loadUrl("javascript:app.waitForArbiterInit(new Function('app.addLayers(" + getLayersJSON(layers, layerIds)
					+ ", " + Boolean.toString(includeDefaultLayer) + ", " + Boolean.toString(defaultLayerVisibility) + ")'))");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void loadMap(CordovaWebView webview, final ArrayList<Layer> layers,
			boolean includeDefaultLayer, boolean defaultLayerVisibility){
		try {
			Log.w("Map", "Map.loadMap defaultLayerVisibility = " + defaultLayerVisibility);
			
			webview.loadUrl("javascript:app.waitForArbiterInit(new Function('app.loadMap(" + getLayersJSON(layers, null)
					+ ", " + Boolean.toString(includeDefaultLayer) + ", " + Boolean.toString(defaultLayerVisibility) + ")'))");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void zoomToExtent(CordovaWebView webview, String extent, String zoomLevel){
		String url = "javascript:app.waitForArbiterInit(new Function('app.zoomToExtent(" 
				+ extent;
		
		if(zoomLevel != null){
			url += ", " + zoomLevel;
		}
		
		url += ")'))";
		
		Log.w("Map", "Map.zoomToExtent: " + url);
		webview.loadUrl(url);
	}
	
	private JSONArray getLayersJSON(ArrayList<Layer> layers, long[] layerIds) throws JSONException{
		JSONArray jsonArray = new JSONArray();
		
		if(layers == null){
			return jsonArray;
		}
		
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
			jsonLayer.put("visibility", layer.isChecked());
			
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
	
	public void toggleLayerVisibility(CordovaWebView webview, long layerId){
		if(layerId == Layer.DEFAULT_FLAG){
			webview.loadUrl("javascript:app.toggleDefaultLayerVisibility()");
		}else{
			webview.loadUrl("javascript:app.toggleLayerVisibility(" 
					+ Long.toString(layerId) + ")");
		}	
	}
}
