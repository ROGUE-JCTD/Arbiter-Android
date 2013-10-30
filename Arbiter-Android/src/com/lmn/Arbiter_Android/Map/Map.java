package com.lmn.Arbiter_Android.Map;

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
	
	public void loadMap(CordovaWebView webview, final Layer[] layers){
		Log.w("MAP", "LOADMAP");
		try {
			webview.loadUrl("javascript:app.loadMap(" 
					+ getLayersJSON(layers) + ")");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private JSONArray getLayersJSON(Layer[] layers) throws JSONException{
		if(layers == null){
			return null;
		}
		
		JSONArray jsonArray = new JSONArray();
		JSONObject layer;
		
		for(int i = 0; i < layers.length; i++){
			layer = new JSONObject();
			layer.put("layerId", layers[i].getLayerId());
			layer.put("featureType", layers[i].getFeatureType());
			layer.put("srs", layers[i].getLayerSRS());
			layer.put("serverUrl", layers[i].getServerUrl());
			
			jsonArray.put(layer);
		}
		
		return jsonArray;
	}
	
	public void deleteLayer(CordovaWebView webview, long layerId){
		webview.loadUrl("javascript:app.removeLayer(" 
			+ Long.toString(layerId) + ")");
			
	}
}
