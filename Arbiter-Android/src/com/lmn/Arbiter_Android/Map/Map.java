package com.lmn.Arbiter_Android.Map;

import java.util.ArrayList;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.lmn.Arbiter_Android.Activities.MapChangeHelper;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.GeometryColumnsHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;

public class Map{
	private Map(){}
	
	private static Map map = null;
	
	public interface MapChangeListener {
		public MapChangeHelper getMapChangeHelper();
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
	
	public void createProject(CordovaWebView webview, final ArrayList<Layer> layers){
		try{
			String url = "javascript:app.waitForArbiterInit(new Function('"
					+ "Arbiter.Cordova.Project.createProject("
					+ getLayersJSON(layers, null) + ")'))";
				
			webview.loadUrl(url);
		} catch (JSONException e){
			e.printStackTrace();
		}
	}
	
	public void addLayers(CordovaWebView webview, final ArrayList<Layer> layers, final long[] layerIds){
		try{
			String url = "javascript:app.waitForArbiterInit(new Function('"
					+ "Arbiter.Cordova.Project.addLayers(" 
					+ getLayersJSON(layers, layerIds) + ")'))";
			
			webview.loadUrl(url);
		} catch (JSONException e){
			e.printStackTrace();
		}
	}
	
	public void setNewProjectsAOI(CordovaWebView webview){
		String url = "javascript:app.waitForArbiterInit(new Function('Arbiter.Cordova.setNewProjectsAOI()'));";
		
		webview.loadUrl(url);
	}
	
	public void setAOI(CordovaWebView webview){
		String url = "javascript:app.waitForArbiterInit(new Function('Arbiter.Cordova.Project.setProjectsAOI()'))";
		
		webview.loadUrl(url);
	}
	
	public void zoomToAOI(CordovaWebView webview){
		String url = "javascript:app.waitForArbiterInit(new Function('Arbiter.Cordova.Project.zoomToAOI()'))";
		
		webview.loadUrl(url);
	}
	
	public void zoomToDefault(CordovaWebView webview){
		String url = "javascript:app.waitForArbiterInit(new Function('Arbiter.Cordova.Project.zoomToDefault()'))";
		
		webview.loadUrl(url);
	}
	
	public void zoomToExtent(CordovaWebView webview, String extent, String zoomLevel){
		String url = "javascript:app.waitForArbiterInit(new Function('Arbiter.Map.zoomToExtent(" 
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
				jsonLayer.put(LayersHelper._ID, layer.getLayerId());
			}else{
				jsonLayer.put(LayersHelper._ID, layerIds[i]);
			}
			
			jsonLayer.put(GeometryColumnsHelper.FEATURE_GEOMETRY_SRID, layer.getSRS());
			jsonLayer.put(LayersHelper.FEATURE_TYPE, layer.getFeatureType());
			jsonLayer.put(LayersHelper.SERVER_ID, layer.getServerId());
			jsonLayer.put(LayersHelper.LAYER_VISIBILITY, layer.isChecked());
			
			jsonArray.put(jsonLayer);
		}
		
		return jsonArray;
	}
	
	public void toggleLayerVisibility(CordovaWebView webview, long layerId){
		String url = "javascript:app.waitForArbiterInit(new Function('";
		if(layerId == Layer.DEFAULT_FLAG){
			//webview.loadUrl("javascript:Arbiter.Layers.toggleDefaultLayerVisibility()");
			url += "Arbiter.Layers.toggleDefaultLayerVisibility()";
		}else{
		//	webview.loadUrl("javascript:Arbiter.Layers.toggleLayerVisibilityById(" 
			//		+ Long.toString(layerId) + ")");
			url += "Arbiter.Layers.toggleLayerVisibilityById(" + Long.toString(layerId) + ")";
		}	
		
		url += "'))";
		
		webview.loadUrl(url);
	}
	
	public void resetWebApp(CordovaWebView webview){
		String url = "javascript:app.waitForArbiterInit(new Function('"
				+ "Arbiter.Cordova.resetWebApp()'))";
		
		webview.loadUrl(url);
	}
	
	public void enterModifyMode(CordovaWebView webview){
		String url = "javascript:app.waitForArbiterInit(new Function('"
				+ "Arbiter.Controls.ControlPanel.enterModifyMode()'))";
		
		webview.loadUrl(url);
	}
	
	public void exitModifyMode(CordovaWebView webview){
		String url = "javascript:app.waitForArbiterInit(new Function('"
				+ "Arbiter.Controls.ControlPanel.exitModifyMode()'))";
		
		webview.loadUrl(url);
	}
	
	public void cancelEdit(CordovaWebView webview){
		String url = "javascript:app.waitForArbiterInit(new Function('"
				+ "Arbiter.Controls.ControlPanel.cancelEdit()'))";
		
		webview.loadUrl(url);
	}
	
	public void getUpdatedGeometry(CordovaWebView webview){
		String url = "javascript:app.waitForArbiterInit(new Function('"
				+ "Arbiter.Cordova.getUpdatedGeometry()'))";
		
		webview.loadUrl(url);
	}
	
	public void unselect(CordovaWebView webview){
		String url = "javascript:app.waitForArbiterInit(new Function('"
				+ "Arbiter.Controls.ControlPanel.unselect()'))";
		
		webview.loadUrl(url);
	}
	
	public void cancelSelection(CordovaWebView webview){
		String url = "javascript:app.waitForArbiterInit(new Function('"
				+ "Arbiter.Controls.ControlPanel.cancelSelection()'))";
		
		webview.loadUrl(url);
	}
	
	public void startInsertMode(CordovaWebView webview, long layerId){
		
		String url = "javascript:app.waitForArbiterInit(new Function('"
				+ "Arbiter.Controls.ControlPanel.startInsertMode("
				+ Long.toString(layerId) + ")'))";
		
		webview.loadUrl(url);
	}
}
