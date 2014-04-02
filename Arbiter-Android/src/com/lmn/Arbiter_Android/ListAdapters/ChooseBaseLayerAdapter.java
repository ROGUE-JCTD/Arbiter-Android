package com.lmn.Arbiter_Android.ListAdapters;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.BaseLayer;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.Dialog.Dialogs.ChooseBaseLayer.BaseLayerUpdater;
import com.lmn.Arbiter_Android.GeometryEditor.GeometryEditor;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChooseBaseLayerAdapter extends BaseAdapter implements ArbiterAdapter<ArrayList<Layer>>{

	private LayoutInflater inflater;
	private int itemLayout;
	private Activity activity;
	private Context context;
	private MapChangeListener mapChangeListener;
	private ArrayList<Layer> layers;
    private boolean creatingProject;
    private BaseLayerUpdater fieldUpdater;
    private BaseLayer selectedBaseLayer;
    
	public ChooseBaseLayerAdapter(Activity activity, int itemLayout, 
			BaseLayerUpdater fieldUpdater, boolean creatingProject, BaseLayer selectedBaseLayer){
		
		this.activity = activity;
		this.context = activity.getApplicationContext();
		this.inflater =	LayoutInflater.from(this.context);
		this.itemLayout = itemLayout;
		this.layers = null;
		this.creatingProject = creatingProject;
		this.fieldUpdater = fieldUpdater;
		this.selectedBaseLayer = selectedBaseLayer;
		
		if(!creatingProject){
			try {
				mapChangeListener = (MapChangeListener) activity;
			} catch (ClassCastException e){
				throw new ClassCastException(activity.toString() 
						+ " must implement MapChangeListener");
			}
		}
	}

	@Override
	public void setData(ArrayList<Layer> layers){
		
		this.layers = layers;
		
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return (layers != null) ? layers.size() : 0;
	}

	@Override
	public Layer getItem(int index) {
		return layers.get(index);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View view = inflater.inflate(itemLayout, null);
		
		final Layer layer = getItem(position);
		
		if(layer != null){
			
            TextView layerNameView = (TextView) view.findViewById(R.id.layerName);
            TextView serverNameView = (TextView) view.findViewById(R.id.serverName);
            
            String layerName = null;
            String serverName = null;
            String serverId = null;
            String featureType = null;
            String layerTitle = layer.getLayerTitle();
            
            Log.w("ChooseBAseLayerADapter", "ChooseBaseLayerAdapter name = " + layer.getLayerTitle());
            
        	if(layerTitle == null || (layerTitle != null && layerTitle.equals("OpenStreetMap"))){
        		layerName = "OpenStreetMap";
        		serverName ="OpenStreetMap";
        		serverId = "OpenStreetMap";
        		featureType = "";
        	}else{
        		layerName = layerTitle;
        		serverName = layer.getServerName();
        		serverId = Integer.toString(layer.getServerId());
        		featureType = layer.getFeatureType();
        	}
        	
        	layerNameView.setText(layerName);
        	serverNameView.setText(serverName);
        	
        	Log.w("ChooseBaseLayer", "ChooseBaseLayer layername = " + layerName + ", url = " 
        			+ layer.getServerUrl() + ", server name = " + serverName + ", serverId = " 
        			+ serverId + ", featureType = " + featureType);
        	final BaseLayer baseLayer = new BaseLayer(layerName, layer.getServerUrl(), serverName, serverId, featureType);
		
			view.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					
					if(!creatingProject && !makeSureNotEditing()){
						return;
					}
					
					final Runnable changeBaseLayer = new Runnable(){
						@Override
						public void run(){
							selectedBaseLayer = baseLayer;
							fieldUpdater.updateBaselayer(baseLayer);
						}
					};
					
					String selectedFeatureType = selectedBaseLayer.getFeatureType(); // null
					String featureType = baseLayer.getFeatureType(); // null
					
					if(selectedFeatureType == null){
						selectedFeatureType = "null";
					}
					
					if(featureType == null){
						featureType = "null";
					}
					
					if(selectedFeatureType != featureType && !selectedFeatureType.equals(featureType)){
						
						if(creatingProject){
							changeBaseLayer.run();
						}else{
							AlertDialog.Builder builder = new AlertDialog.Builder(activity);
							
							String title = activity.getResources().getString(R.string.warning);
							String message = activity.getResources().getString(R.string.change_baselayer_warning);
							
							builder.setTitle(title);
							builder.setMessage(message);
							builder.setNegativeButton(android.R.string.cancel, null);
							builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
		
								@Override
								public void onClick(DialogInterface dialog, int which) {
									changeBaseLayer.run();
								}
							});
							
							builder.create().show();
						}
					}
				}
			});
		}
		
		return view;
	}
	
	// Return true if not editing
    private boolean makeSureNotEditing(){
    	
		int editMode = mapChangeListener.getMapChangeHelper().getEditMode();
		
		if(editMode == GeometryEditor.Mode.OFF || editMode == GeometryEditor.Mode.SELECT){
			return true;
		}
			
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(R.string.finish_editing_title);
		builder.setMessage(R.string.finish_editing_message);
		builder.setIcon(R.drawable.icon);
		builder.setPositiveButton(android.R.string.ok, null);
		
		builder.create().show();
		
		return false;
    }

	@Override
	public long getItemId(int position) {
		return position;
	}
}
