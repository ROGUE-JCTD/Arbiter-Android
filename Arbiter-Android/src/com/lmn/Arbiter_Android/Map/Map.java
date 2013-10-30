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
	
	public static Map getMap(){
		if(map == null){
			map = new Map();
		}
		
		return map;
	}
	
	public void loadMap(CordovaWebView webview, final ArrayList<Layer> layers, boolean includeDefaultLayer){
		Log.w("MAP", "LOADMAP");
		try {
			webview.loadUrl("javascript:app.loadMap(" 
					+ getLayersJSON(layers) + ", " + Boolean.toString(includeDefaultLayer)  + ")");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private JSONArray getLayersJSON(ArrayList<Layer> layers) throws JSONException{
		if(layers == null){
			return null;
		}
		
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonLayer;
		Layer layer;
		
		for(int i = 0; i < layers.size(); i++){
			layer = layers.get(i);
			jsonLayer = new JSONObject();
			jsonLayer.put("layerId", layer.getLayerId());
			jsonLayer.put("featureType", layer.getFeatureType());
			jsonLayer.put("srs", layer.getLayerSRS());
			jsonLayer.put("serverUrl", layer.getServerUrl());
			
			jsonArray.put(jsonLayer);
		}
		
		return jsonArray;
	}
	
	public void deleteLayer(CordovaWebView webview, long layerId){
		if(layerId == -1){
			webview.loadUrl("javascript:app.removeDefaultLayer()");
		}else{
			webview.loadUrl("javascript:app.removeLayer(" 
					+ Long.toString(layerId) + ")");
		}	
	}
}
