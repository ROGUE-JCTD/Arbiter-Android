package com.lmn.Arbiter_Android;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.lmn.Arbiter_Android.BaseClasses.Layer;

public class Map {
	private CordovaWebView webview;
	
	public Map(CordovaWebView webview){
		this.webview = webview;
	}
	
	public void loadMap(Layer[] layers){
		try {
			webview.loadUrl("javascript:Arbiter.waitForInit(new Function(app.loadMap(" 
					+ getLayersJSON(layers) + ")))");
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
}
