package com.lmn.Arbiter_Android.ListAdapters;

import com.lmn.Arbiter_Android.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ServerTypesAdapter extends BaseAdapter{

	private String[] types;
	private LayoutInflater inflater;
	
	public static class Types {
		public static final String WMS = "WMS";
		public static final String TMS = "TMS";
	}
	
	public static class Hints {
		public static final String WMS = "http://url/wms";
		public static final String TMS = "http://url/1.0.0/";
	}
	
	public ServerTypesAdapter(Activity activity){
		
		this.inflater = activity.getLayoutInflater();
		
		types = new String[2];
		
		types[0] = Types.WMS;
		types[1] = Types.TMS;
	}
	
	public int getPositionFromType(String type){
		
		if(type.equals(Types.WMS)){
			return 0;
		}
		
		return 1;
	}
	
	@Override
	public int getCount() {
		return types.length;
	}

	@Override
	public String getItem(int position) {
		return types[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View view = convertView;
		
		if(view == null){
			view = inflater.inflate(R.layout.spinner_item, null);
		}
		
		TextView textView = (TextView) view.findViewById(R.id.spinnerText);
		
		textView.setText(getItem(position));
		
		return view;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent){
		
		View view = convertView;
		
		if(view == null){
			view = inflater.inflate(R.layout.drop_down_item, null);
		}
		
		TextView textView = (TextView) view.findViewById(R.id.spinnerText);
		
		textView.setText(getItem(position));
		
		return view;
	}
}
