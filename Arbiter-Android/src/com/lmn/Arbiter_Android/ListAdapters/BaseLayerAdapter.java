package com.lmn.Arbiter_Android.ListAdapters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lmn.Arbiter_Android.BaseClasses.BaseLayer;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BaseLayerAdapter extends BaseAdapter implements ArbiterAdapter<JSONArray>{

	private JSONArray baselayers;
	private LayoutInflater inflater;
	private int itemLayout;
	private int textId;
	
	public BaseLayerAdapter(FragmentActivity activity, int itemLayout, int textId){
		
		this.inflater = LayoutInflater.from(activity.getApplicationContext());
		this.itemLayout = itemLayout;
		this.textId = textId;
	}
	
	@Override
	public int getCount() {
		return (baselayers != null) ? baselayers.length() : 0;
	}

	@Override
	public JSONObject getItem(int position) {
		JSONObject baselayer = null;
		
		try {
			baselayer = baselayers.getJSONObject(position);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return baselayer;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View view = convertView;
		
		if(view == null){
			view = inflater.inflate(itemLayout, null);
		}
		
		BaseLayer baseLayer = new BaseLayer(getItem(position));
		
		if(baseLayer != null){
			TextView layerName = (TextView) view.findViewById(textId);
		
			if(layerName != null){
				layerName.setText(baseLayer.getName());
			}
		}
		
		return view;
	}

	@Override
	public void setData(JSONArray data) {
		
		this.baselayers = data;
		
		notifyDataSetChanged();
	}

}
