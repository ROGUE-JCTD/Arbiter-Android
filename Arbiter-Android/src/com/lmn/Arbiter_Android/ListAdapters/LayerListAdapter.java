package com.lmn.Arbiter_Android.ListAdapters;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class LayerListAdapter extends BaseAdapter{

	private LayerChangeListener layerChangeListener;
	
	public interface LayerChangeListener {
		public void onLayerDeleted(long layerId);
		
		public void onLayerVisibilityChanged(long layerId);
	}
	
	private Layer[] items;
	private final LayoutInflater inflater;
	private int itemLayout;
	private final FragmentActivity activity;
	private final Context context;
	
	public LayerListAdapter(FragmentActivity activity, int itemLayout){
		
		this.context = activity.getApplicationContext();
		this.inflater = LayoutInflater.from(this.context);
		this.items = new Layer[0];
		this.itemLayout = itemLayout;
		this.activity = activity;
		
		try {
			layerChangeListener = (LayerChangeListener) activity;
		} catch (ClassCastException e){
			throw new ClassCastException(activity.toString() 
					+ " must implement LayerChangeListener");
		}
	}
	
	public void setData(Layer[] data){
		items = data;
		
		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		// Inflate the layout
		if(view == null){
			view = inflater.inflate(itemLayout, null);
		}
		
		final Layer listItem = items[position];
		
		if(listItem != null){
            TextView layerName = (TextView) view.findViewById(R.id.layerName);
            TextView serverName = (TextView) view.findViewById(R.id.serverName);
            ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteLayer);
            
            if(layerName != null){
            	layerName.setText(listItem.getLayerTitle());
            }
            
            if(serverName != null){
            	serverName.setText(listItem.getServerName());
            }
            
            if(deleteButton != null){
            	deleteButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						final Layer layer = new Layer(listItem);
						
						CommandExecutor.runProcess(new Runnable(){
							@Override
							public void run() {
								final long layerId = layer.getLayerId();
								GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(context);
								LayersHelper.getLayersHelper().delete(
										helper.getWritableDatabase(), context, layer, new Runnable(){

											@Override
											public void run() {
												activity.runOnUiThread(new Runnable(){

													@Override
													public void run() {
														layerChangeListener.onLayerDeleted(layerId);
													}
												});
												
											}
											
										});
							}
						});
					}
            		
            	});
            }
		}
		
		return view;
	}
	
	@Override
	public int getCount() {
		if(items == null){
			return 0;
		}
		
		return items.length;
	}

	@Override
	public Layer getItem(int position) {
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public Layer[] getLayers(){
		return items;
	}
}
