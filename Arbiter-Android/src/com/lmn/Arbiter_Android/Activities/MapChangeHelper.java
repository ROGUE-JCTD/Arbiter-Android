package com.lmn.Arbiter_Android.Activities;

import java.util.ArrayList;

import org.apache.cordova.CordovaWebView;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.CordovaPlugins.Helpers.FeatureHelper;
import com.lmn.Arbiter_Android.Map.Map;

public class MapChangeHelper {
	private FragmentActivity activity;
	private CordovaWebView cordovaWebView;
	private IncompleteProjectHelper incompleteProjectHelper;
	
	public MapChangeHelper(FragmentActivity activity, CordovaWebView cordovaWebView,
			IncompleteProjectHelper incompleteProjectHelper){
		
		this.activity = activity;
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
	
	public void onProjectCreated(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				incompleteProjectHelper.checkForAOI();
			}
		});
	}
	
	public void onLayersAdded(final ArrayList<Layer> layers, final long[] layerIds,
			final String includeDefaultLayer, final String defaultLayerVisibility) {
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Map.getMap().addLayers(cordovaWebView, layers, layerIds);
			}
		});
	}
	
	public void onSyncCompleted(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				incompleteProjectHelper.toggleComplete(true);
			}
		});
	}
	
	public void onServerDeleted(long serverId){
		reloadMap();
	}
	
	private void toggleEditButtons(final boolean visible){
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
	
	public void onEditFeature(final Feature feature){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				toggleEditButtons(true);
				
				Map.getMap().enterModifyMode(cordovaWebView);
			}
		});
	}
	
	public void doneEditingFeature(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				toggleEditButtons(false);
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
	
	public void cancelEditing(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Map.getMap().cancelSelection(cordovaWebView);
			}
		});
	}
	
	private void setInsertFeatureText(String featureType){
		TextView insertLayerText = (TextView)
				activity.findViewById(R.id.insertLayerText);
		
		insertLayerText.setText(featureType);
	}
	
	private void toggleInsertFeatureBar(boolean makeVisible){
		View insertFeatureBar = activity.findViewById(R.id.insertFeatureBar);
		
		if(makeVisible){
			insertFeatureBar.setVisibility(View.VISIBLE);
		}else{
			insertFeatureBar.setVisibility(View.GONE);
		}
	}
	
	public void startInsertMode(final String featureType, final long layerId){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Map.getMap().startInsertMode(cordovaWebView, layerId);
				
				setInsertFeatureText(featureType);
				
				toggleInsertFeatureBar(true);
			}
		});
	}
	
	public void endInsertMode(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Map.getMap().resetWebApp(cordovaWebView);
				
				toggleInsertFeatureBar(false);	
			}
		});
	}
	
	public void doneInsertingFeature(){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				FeatureHelper helper = new FeatureHelper(activity);
    			
    			helper.displayUpdatedFeature();
				
				toggleInsertFeatureBar(false);
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
}
