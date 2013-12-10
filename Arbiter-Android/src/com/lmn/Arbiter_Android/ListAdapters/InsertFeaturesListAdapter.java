package com.lmn.Arbiter_Android.ListAdapters;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class InsertFeaturesListAdapter extends BaseAdapter implements ArbiterAdapter<ArrayList<Layer>>{

	private MapChangeListener mapChangeListener;
	
	private ArrayList<Layer> items;
	private final LayoutInflater inflater;
	private int itemLayout;
	private Context context;
	private DialogFragment dialog;
	
	public InsertFeaturesListAdapter(DialogFragment dialog, int itemLayout){
		
		this.context = dialog.getActivity().getApplicationContext();
		this.inflater = LayoutInflater.from(this.context);
		this.items = new ArrayList<Layer>();
		this.itemLayout = itemLayout;
		this.dialog = dialog;
		
		try {
			mapChangeListener = (MapChangeListener) dialog.getActivity();
		} catch (ClassCastException e){
			throw new ClassCastException(dialog.getActivity().toString() 
					+ " must implement MapChangeListener");
		}
	}
	
	public void setData(ArrayList<Layer> data){
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
		
		final Layer layer = getItem(position);
		
		if(layer != null){
            TextView layerNameView = (TextView) view.findViewById(R.id.layerName);
            TextView serverNameView = (TextView) view.findViewById(R.id.serverName);
            
            if(layerNameView != null){
            	layerNameView.setText(layer.getLayerTitle());
            }
            
            if(serverNameView != null){
            	serverNameView.setText(layer.getServerName());
            }
		}
		
		view.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				mapChangeListener.getMapChangeHelper().startInsertMode(layer
						.getFeatureTypeNoPrefix(), layer.getLayerId());
				
				dialog.dismiss();
			}
		});
		
		return view;
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
