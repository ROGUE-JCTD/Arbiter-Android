package com.lmn.Arbiter_Android.ListAdapters;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;
import com.lmn.Arbiter_Android.Loaders.LayersListLoader;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class LayerListAdapter extends BaseAdapter{

	private MapChangeListener mapChangeListener;
	
	private ArrayList<Layer> items;
	private final LayoutInflater inflater;
	private int itemLayout;
	private final FragmentActivity activity;
	private final Context context;
	
	public LayerListAdapter(FragmentActivity activity, int itemLayout){
		
		this.context = activity.getApplicationContext();
		this.inflater = LayoutInflater.from(this.context);
		this.items = new ArrayList<Layer>();
		this.itemLayout = itemLayout;
		this.activity = activity;
		
		try {
			mapChangeListener = (MapChangeListener) activity;
		} catch (ClassCastException e){
			throw new ClassCastException(activity.toString() 
					+ " must implement MapChangeListener");
		}
	}
	
	private void addDefaultLayer(ArrayList<Layer> layers){
		if(layers != null){
			if(ArbiterProject.getArbiterProject().includeDefaultLayer()){
				layers.add(new Layer(Layer.DEFAULT_FLAG, null, Server.DEFAULT_FLAG, null, null,
						Layer.DEFAULT_LAYER_NAME, null, null, 
						ArbiterProject.getArbiterProject().getDefaultLayerVisibility()));
				layers.get(layers.size() - 1).setIsDefaultLayer(true);
			}
		}
	}
	
	public void setData(ArrayList<Layer> data){
		items = data;
		
		addDefaultLayer(items);

		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		// Inflate the layout
		if(view == null){
			view = inflater.inflate(itemLayout, null);
		}
		
		final Layer listItem = getItem(position);
		
		if(listItem != null){
            TextView layerNameView = (TextView) view.findViewById(R.id.layerName);
            TextView serverNameView = (TextView) view.findViewById(R.id.serverName);
            ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteLayer);
            ToggleButton layerVisibility = (ToggleButton) view.findViewById(R.id.layerVisibility);
            
            final boolean isDefaultLayer = listItem.isDefaultLayer();
            
            if(layerNameView != null){
            	layerNameView.setText(listItem.getLayerTitle());
            }
            
            if(serverNameView != null){
            	serverNameView.setText((isDefaultLayer) ? Server.DEFAULT_SERVER_NAME : listItem.getServerName());
            }
            
            if(deleteButton != null){
            	deleteButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						if(!isDefaultLayer){
							deleteLayer(new Layer(listItem));
						}else{
							deleteDefaultLayer();
						}
					}
            		
            	});
            }
            
            if(layerVisibility != null){
            	// Set the toggle to its appropriate position
            	layerVisibility.setChecked(listItem.isChecked());
                
            	layerVisibility.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						listItem.setChecked(!listItem.isChecked());
						if(isDefaultLayer){
							updateDefaultLayerVisibility(listItem.getLayerId(), listItem.isChecked());
						}else{
							updateLayerVisibility(listItem.getLayerId(), listItem.isChecked()); 
						}
					}
				});
            }
		}
		
		return view;
	}
	
	private void updateDefaultLayerVisibility(final long layerId, final boolean visibility){
		final long projectId = ArbiterProject.getArbiterProject().getOpenProject(context);
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(context);
				ProjectsHelper.getProjectsHelper().setDefaultLayerVisibility(
						helper.getWritableDatabase(), context, projectId, visibility, new Runnable(){
							@Override
							public void run(){
								ArbiterProject.getArbiterProject().setDefaultLayerVisibility(visibility);
								
								mapChangeListener.onLayerVisibilityChanged(layerId);
							}
						});
			}
		});
	}
	
	private void updateLayerVisibility(final long layerId, final boolean visibility){
		final ContentValues values = new ContentValues();
		
		values.put(LayersHelper.LAYER_VISIBILITY, visibility);
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(context);
				LayersHelper.getLayersHelper().updateAttributeValues(helper.getWritableDatabase(), context, layerId, values, new Runnable(){
					@Override
					public void run(){
						mapChangeListener.onLayerVisibilityChanged(layerId);
					}
				});
			}
		});
	}
	
	private void deleteDefaultLayer(){
		ArbiterProject.getArbiterProject().setIncludeDefaultLayer(context, false, new Runnable(){
			@Override
			public void run(){
				mapChangeListener.onLayerDeleted(Layer.DEFAULT_FLAG);
				
				LocalBroadcastManager.getInstance(context).
					sendBroadcast(new Intent(LayersListLoader.LAYERS_LIST_UPDATED));
			}
		});
	}
	
	private void deleteLayer(final Layer layer){
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run() {
				final long layerId = layer.getLayerId();
				GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(context);
				LayersHelper.getLayersHelper().delete(
						helper.getWritableDatabase(), context, layer, new Runnable(){

							@Override
							public void run() {
								mapChangeListener.onLayerDeleted(layerId);
							}
							
						});
			}
		});
	}
	
	@Override
	public int getCount() {
		if(items == null){
			return 0;
		}
		
		return items.size();
	}

	@Override
	public Layer getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public ArrayList<Layer> getLayers(){
		return items;
	}
}
