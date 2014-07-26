package com.lmn.Arbiter_Android.Activities;

import java.util.ArrayList;

import org.apache.cordova.CordovaWebView;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.CookieManager.ArbiterCookieManager;
import com.lmn.Arbiter_Android.Dialog.ProgressDialog.SyncProgressDialog;
import com.lmn.Arbiter_Android.GeometryEditor.GeometryEditor;
import com.lmn.Arbiter_Android.Map.Map;

public class MapChangeHelper {
	private FragmentActivity activity;
	private CordovaWebView cordovaWebView;
	private IncompleteProjectHelper incompleteProjectHelper;
	private GeometryEditor editor;
	
	public MapChangeHelper(FragmentActivity activity, CordovaWebView cordovaWebView,
			IncompleteProjectHelper incompleteProjectHelper){
		
		this.activity = activity;
		this.editor = new GeometryEditor(activity);
		this.cordovaWebView = cordovaWebView;
		this.incompleteProjectHelper = incompleteProjectHelper;
	}
	
	/**
	 * LayerChangeListener events
	 */
	public void onLayerDeleted(final long layerId) {
		reloadMap();
	}

	public void onLayerVisibilityChanged(final long layerId) {
		reloadMap();
	}
	
	public void onLayerOrderChanged(){
		reloadMap();
	}
	
	public void onSyncCompleted(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				incompleteProjectHelper.checkForAOI();
			}
		});
	}
	
	public void onServerUpdated(){
		reloadMap();
	}
	
	public void onLayersAdded(final ArrayList<Layer> layers, final long[] layerIds, final HasThreadPool hasThreadPool) {
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(layers.size() > 0){
					SyncProgressDialog.show(activity);
				}
				
				hasThreadPool.getThreadPool().execute(new Runnable(){
					@Override
					public void run(){
					
						new ArbiterCookieManager(activity.getApplicationContext()).updateAllCookies();
						
						activity.runOnUiThread(new Runnable(){
							@Override
							public void run(){
								Map.getMap().addLayers(cordovaWebView, layers, layerIds);
							}
						});
					}
				});
			}
		});
	}
	
	public void onServerDeleted(long serverId){
		reloadMap();
	}
	
	public void onUnselectFeature(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Log.w("MapChangeHelper", "MapChangeHelper onUnselectFeature");
				
				if(editor.getEditMode() != GeometryEditor.Mode.INSERT){
					editor.setEditMode(GeometryEditor.Mode.OFF);
				}
			}
		});
	}
	
	public void onSelectFeature(final String featureType, final String featureId,
			final String layerId, final String wktGeometry, final String mode){
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				editor.setFeatureInfo(featureType, featureId, layerId, wktGeometry, new Runnable(){
					@Override
					public void run(){
						
						Log.w("MapChangeHelper", "MapChangeHelper onSelectFEature featureID = " + featureId);
						
						if(featureId != null && !"null".equals(featureId)){
							editor.setEditMode(GeometryEditor.Mode.SELECT);
						}else{
							editor.setEditMode(GeometryEditor.Mode.INSERT);
						}
					}
				});
			}
		});
	}
	
	public void unselect(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Map.getMap().unselect(cordovaWebView);
			}
		});
	}
	
	public void exitModifyMode(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Map.getMap().exitModifyMode(cordovaWebView);
			}
		});
	}
	
	public void startInsertMode(final String featureType, final long layerId, final String geometryType){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Map.getMap().startInsertMode(cordovaWebView, layerId, geometryType);
				
				editor.setEditMode(GeometryEditor.Mode.INSERT);
			}
		});
	}
	
	public void startAddPartMode(final String geometryType){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Map.getMap().addGeometry(cordovaWebView, geometryType);
				
				//editor.setEditMode(GeometryEditor.Mode.INSERT);
			}
		});
	}
	
	public void endInsertMode(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Map.getMap().resetWebApp(cordovaWebView);
				
				//toggleInsertFeatureBar(false);
				
				editor.setEditMode(GeometryEditor.Mode.OFF);
			}
		});
	}
	
	public void doneInsertingFeature(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				//toggleInsertFeatureBar(false);
				
				editor.setEditMode(GeometryEditor.Mode.OFF);
			}
		});
	}
	
	public void reloadMap(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Map.getMap().resetWebApp(cordovaWebView);
			}
		});
	}
	
	public void enableMultiPartBtns(final boolean enable, final boolean enableCollection){
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				editor.enableMultiPartBtns(enable, enableCollection);
			}
		});
	}
	
	public int getEditMode(){
		return editor.getEditMode();
	}
	
	public void setEditMode(final int mode){
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				editor.setEditMode(mode);;
			}
		});
	}
	
	public void showUpdatedGeometry(String featureType, String featureId, String layerId, String wktGeometry){
		editor.showUpdatedGeometry(featureType, featureId, layerId, wktGeometry);
	}
	
	public void hidePartButtons(){
		editor.hidePartButtons();
	}
	
	public void cacheBaseLayer(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Map.getMap().cacheBaseLayer(cordovaWebView);
			}
		});
	}
}
