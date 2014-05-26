package com.lmn.Arbiter_Android.Map;

import java.util.ArrayList;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.lmn.Arbiter_Android.Activities.MapChangeHelper;
import com.lmn.Arbiter_Android.AppFinishedLoading.AppFinishedLoading;
import com.lmn.Arbiter_Android.AppFinishedLoading.AppFinishedLoadingJob;
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
	
	public void goToProjects(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('Arbiter.Cordova.goToProjects()'));";
				webview.loadUrl(url);
			}
		});
	}
	
	public void createNewProject(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('Arbiter.Cordova.createNewProject()'));";
				webview.loadUrl(url);
			}
		});
	}
	
	public void createProject(final CordovaWebView webview, final ArrayList<Layer> layers, final long[] layerIds){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				try{
					String url = "javascript:app.waitForArbiterInit(new Function('"
							+ "Arbiter.Cordova.Project.createProject("
							+ getLayersJSON(layers, layerIds) + ")'))";
						
					webview.loadUrl(url);
				} catch (JSONException e){
					e.printStackTrace();
				}
			}
		});
	}
	
	public void addLayers(final CordovaWebView webview, final ArrayList<Layer> layers, final long[] layerIds){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				try{
					String url = "javascript:app.waitForArbiterInit(new Function('"
							+ "Arbiter.Cordova.Project.addLayers(" 
							+ getLayersJSON(layers, layerIds) + ")'))";
					
					webview.loadUrl(url);
				} catch (JSONException e){
					e.printStackTrace();
				}
			}
		});
	}
	
	public void getTileCount(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('Arbiter.Cordova.getTileCount()'));";
				
				webview.loadUrl(url);
			}
		});
	}

	public void setNewProjectsAOI(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('Arbiter.Cordova.setNewProjectsAOI()'));";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void setAOI(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('Arbiter.Cordova.setProjectsAOI()'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void zoomToAOI(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('Arbiter.Cordova.Project.zoomToAOI()'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void zoomToDefault(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('Arbiter.Cordova.Project.zoomToDefault()'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void zoomToExtent(final CordovaWebView webview, final String extent, final String zoomLevel){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('Arbiter.Map.zoomToExtent(" 
						+ extent;
				
				if(zoomLevel != null){
					url += ", " + zoomLevel;
				}
				
				url += ")'))";
				
				Log.w("Map", "Map.zoomToExtent: " + url);
				webview.loadUrl(url);
			}
		});
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
				Log.w("Map", "Map layer featureType = " + layer.getFeatureType() + "id = " + layer.getLayerId());
				jsonLayer.put(LayersHelper._ID, layer.getLayerId());
			}else{
				Log.w("Map", "Map layer featureType = " + layer.getFeatureType() + "id = " + layerIds[i]);
				jsonLayer.put(LayersHelper._ID, layerIds[i]);
			}
			
			jsonLayer.put(GeometryColumnsHelper.FEATURE_GEOMETRY_SRID, layer.getSRS());
			jsonLayer.put(LayersHelper.FEATURE_TYPE, layer.getFeatureType());
			jsonLayer.put(LayersHelper.SERVER_ID, layer.getServerId());
			jsonLayer.put(LayersHelper.LAYER_VISIBILITY, layer.isChecked());
			jsonLayer.put(LayersHelper.LAYER_TITLE, layer.getLayerTitle());
			
			jsonArray.put(jsonLayer);
		}
		
		return jsonArray;
	}
	
	public void toggleLayerVisibility(final CordovaWebView webview, final long layerId){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('";
				
				url += "Arbiter.Layers.toggleLayerVisibilityById(" + Long.toString(layerId) + ")";
				
				url += "'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void resetWebApp(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Cordova.resetWebApp()'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void enterModifyMode(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Controls.ControlPanel.enterModifyMode()'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void exitModifyMode(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Controls.ControlPanel.exitModifyMode()'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void cancelEdit(final CordovaWebView webview, final String originalGeometry){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Controls.ControlPanel.cancelEdit(\"" + originalGeometry + "\")'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void getUpdatedGeometry(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Cordova.getUpdatedGeometry()'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void unselect(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Controls.ControlPanel.unselect()'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void startInsertMode(final CordovaWebView webview, final long layerId, final String geometryType){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Controls.ControlPanel.startInsertMode("
						+ Long.toString(layerId) + ", \"" + geometryType + "\")'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void sync(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Cordova.Project.sync()'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void zoomToCurrentPosition(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Cordova.Project.zoomToCurrentPosition()'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void updateAOI(final CordovaWebView webview, final String aoi){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Cordova.Project.updateAOI(" + aoi + ")'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void zoomIn(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Map.getMap().zoomIn()'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void zoomOut(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Map.getMap().zoomOut()'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void zoomToFeature(final CordovaWebView webview, final String layerId, final String fid){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "app.zoomToFeature(\"" + layerId + "\",\"" + fid + "\")'))";
				
				Log.w("Map", url);
				webview.loadUrl(url);
			}
		});
	}
	
	public void finishGeometry(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Controls.ControlPanel.finishGeometry()'))";
				
				webview.loadUrl(url);
			}
		});
	}
	
	public void addPart(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Controls.ControlPanel.addPart()'))";
				
				webview.loadUrl(url);
			}
		});
	}

	public void removePart(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Controls.ControlPanel.removePart()'))";
			
				webview.loadUrl(url);
			}
		});
	}

	public void addGeometry(final CordovaWebView webview, final String type){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Controls.ControlPanel.addGeometry(\"" + type + "\")'))";
				
				webview.loadUrl(url);
			}
		});
	}

	public void removeGeometry(final CordovaWebView webview){
	
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Controls.ControlPanel.removeGeometry()'))";
			
				webview.loadUrl(url);
			}
		});
	}
	
	public void cacheBaseLayer(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Cordova.Project.cacheBaseLayer()'))";
			
				webview.loadUrl(url);
			}
		});
	}
	
	public void getNotifications(final CordovaWebView webview, final String syncId){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Cordova.Project.getNotifications(\"" + syncId + "\")'))";
			
				webview.loadUrl(url);
			}
		});
	}
	
	public void showWMSLayersForServer(final CordovaWebView webview, final String serverId){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "app.showWMSLayersForServer(\"" + serverId + "\")'))";
			
				webview.loadUrl(url);
			}
		});
	}
	
	public void checkIsAlreadyAddingGeometryPart(final CordovaWebView webview){
		
		AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
			@Override
			public void run(){
				
				String url = "javascript:app.waitForArbiterInit(new Function('"
						+ "Arbiter.Controls.ControlPanel.isAddingPart()'))";
			
				webview.loadUrl(url);
			}
		});
	}
}
