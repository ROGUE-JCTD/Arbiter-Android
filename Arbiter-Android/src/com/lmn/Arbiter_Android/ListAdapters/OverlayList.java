package com.lmn.Arbiter_Android.ListAdapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.GeometryEditor.GeometryEditor;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;
import com.lmn.Arbiter_Android.OrderLayers.OrderLayersModel;
import com.lmn.Arbiter_Android.OrderLayers.OrderLayersModelException;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class OverlayList extends CustomList<ArrayList<Layer>, Layer> {

	private LayoutInflater inflater;
	private int itemLayout;
	private Activity activity;
	private Context context;
	private ArbiterProject arbiterProject;
	private OrderLayersModel orderLayersModel;
	private MapChangeListener mapChangeListener;
	
	private static final Map<String, String> COLOR_MAP;
    static {
        Map<String, String> aMap = new HashMap<String,String>();
        aMap.put("teal","#008080");
		aMap.put("maroon","#800000");
		aMap.put("green","#008000");
		aMap.put("purple","#800080");
		aMap.put("fuchsia","#FF00FF");
		aMap.put("lime","#00FF00");
		aMap.put("red","#FF0000");
		aMap.put("black","#000000");
		aMap.put("navy","#000080");
		aMap.put("aqua","#00FFFF");
		aMap.put("grey","#808080");
		aMap.put("olive","#808000");
		aMap.put("yellow","#FFFF00");
		aMap.put("silver","#C0C0C0");
		aMap.put("white","#FFFFFF");
		COLOR_MAP = Collections.unmodifiableMap(aMap);
    }
    
	public OverlayList(ViewGroup viewGroup, Activity activity, int itemLayout){
		super(viewGroup);
		
		this.activity = activity;
		this.context = activity.getApplicationContext();
		this.inflater =	LayoutInflater.from(this.context);
		this.itemLayout = itemLayout;
		this.arbiterProject = ArbiterProject.getArbiterProject();
		
		try {
			this.orderLayersModel = new OrderLayersModel(this);
		} catch (OrderLayersModelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			mapChangeListener = (MapChangeListener) activity;
		} catch (ClassCastException e){
			throw new ClassCastException(activity.toString() 
					+ " must implement MapChangeListener");
		}
	}

	@Override
	public void setData(ArrayList<Layer> layers){
		super.setData(layers);
		
		this.orderLayersModel.setLayers(layers);
	}
	
	@Override
	public int getCount() {
		return getData().size();
	}

	@Override
	public Layer getItem(int index) {
		return getData().get(index);
	}

	@Override
	public View getView(final int position) {
		
		View view = inflater.inflate(itemLayout, null);
		
		final Layer layer = getItem(position);
		
		if(layer != null){
			if(layer.getColor() != null) {
				View layerColorView = view.findViewById(R.id.layerColor);
				
				if(layerColorView != null){
					layerColorView.setBackgroundColor(Color.parseColor(COLOR_MAP.get(layer.getColor())));
				}
			}
            TextView layerNameView = (TextView) view.findViewById(R.id.layerName);
            TextView serverNameView = (TextView) view.findViewById(R.id.serverName);
            ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteLayer);
            ToggleButton layerVisibility = (ToggleButton) view.findViewById(R.id.layerVisibility);
            ImageButton moveLayerUp = (ImageButton) view.findViewById(R.id.moveLayerUp);
            ImageButton moveLayerDown = (ImageButton) view.findViewById(R.id.moveLayerDown);
            
            if(layerNameView != null){
            	layerNameView.setText(layer.getLayerTitle());
            }
            
            if(serverNameView != null){
            	serverNameView.setText(layer.getServerName());
            }
            
            if(deleteButton != null){
            	deleteButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						if(makeSureNotEditing()){
							deleteLayer(new Layer(layer));
						}
					}
            		
            	});
            }
            
            if(layerVisibility != null){
            	// Set the toggle to its appropriate position
            	layerVisibility.setChecked(layer.isChecked());
                
            	layerVisibility.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						layer.setChecked(!layer.isChecked());
						
						updateLayerVisibility(layer.getLayerId(), layer.isChecked()); 
					}
				});
            }
            
            if(moveLayerUp != null){
            	
            	if(position == 0){
            		moveLayerUp.setVisibility(View.GONE);
            	}else{
            		moveLayerUp.setVisibility(View.VISIBLE);
            	}
            	
            	moveLayerUp.setOnClickListener(new OnClickListener(){
            		@Override
            		public void onClick(View v){
            			orderLayersModel.moveLayerUp(position);
            		}
            	});
            }
            
            if(moveLayerDown != null){
            	
            	if(position == (getCount() - 1)){
            		moveLayerDown.setVisibility(View.GONE);
            	}else{
            		moveLayerDown.setVisibility(View.VISIBLE);
            	}
            	
            	moveLayerDown.setOnClickListener(new OnClickListener(){
            		@Override
            		public void onClick(View v){
            			orderLayersModel.moveLayerDown(position);
            		}
            	});
            }
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
    
	private void updateLayerVisibility(final long layerId, final boolean visibility){
		final ContentValues values = new ContentValues();
		final String projectName = arbiterProject.getOpenProject(activity);
		
		values.put(LayersHelper.LAYER_VISIBILITY, visibility);
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				ProjectDatabaseHelper helper = ProjectDatabaseHelper.
						getHelper(context, ProjectStructure
								.getProjectPath(projectName), false);
				
				LayersHelper.getLayersHelper().updateAttributeValues(helper.getWritableDatabase(), context, layerId, values, new Runnable(){
					@Override
					public void run(){
						mapChangeListener.getMapChangeHelper()
							.onLayerVisibilityChanged(layerId);
					}
				});
			}
		});
	}
	
	private void deleteLayer(final Layer layer){
		
		final String projectName = arbiterProject.getOpenProject(activity);
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run() {
				
				String path = ProjectStructure.getProjectPath(projectName);
				
				ProjectDatabaseHelper projectHelper = ProjectDatabaseHelper.
						getHelper(context, path, false);
				
				FeatureDatabaseHelper featureHelper = 
						FeatureDatabaseHelper.getHelper(context, path, false);
				
				LayersHelper.getLayersHelper().delete(
					projectHelper.getWritableDatabase(), 
					featureHelper.getWritableDatabase(), 
					context, 
					layer
				);
					
				mapChangeListener.getMapChangeHelper()
					.onLayerDeleted(layer.getLayerId());
			}
		});
	}
	
	public void setItemLayout(int itemLayout){
		this.itemLayout = itemLayout;
	}
	
	public OrderLayersModel getOrderLayersModel(){
		return this.orderLayersModel;
	}
}
