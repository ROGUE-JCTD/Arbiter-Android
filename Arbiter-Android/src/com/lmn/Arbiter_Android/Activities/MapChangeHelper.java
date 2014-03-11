package com.lmn.Arbiter_Android.Activities;

import java.util.ArrayList;

import org.apache.cordova.CordovaWebView;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
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
	
	public void onLayersAdded(final ArrayList<Layer> layers, final long[] layerIds) {
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(layers.size() > 0){
					SyncProgressDialog.show(activity);
				}
				
				Map.getMap().addLayers(cordovaWebView, layers, layerIds);
			}
		});
	}
	
	public void onServerDeleted(long serverId){
		reloadMap();
	}
	
	public void toggleEditButtons(final boolean visible){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				RelativeLayout layout = (RelativeLayout) activity
						.findViewById(R.id.editFeatureButtons);
				
				if(visible){
					layout.setVisibility(View.VISIBLE);
				}else{
					layout.setVisibility(View.GONE);
				}
			}
		});
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
						if(featureId != null && featureId != "null"){
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
				
				editor.setEditMode(GeometryEditor.Mode.INSERT);
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
	
	public void enableDoneEditingButton(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				editor.enableDoneButton();
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
	
	public void setEditMode(final int mode){
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				editor.setEditMode(mode);;
			}
		});
	}
	
	public void saveUpdatedGeometry(String featureType, String featureId, String layerId, String wktGeometry){
		editor.saveUpdatedGeometry(featureType, featureId, layerId, wktGeometry);
	}
}
