package com.lmn.Arbiter_Android.ListAdapters;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ListItems.AddLayersListItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class AddLayersListAdapter extends ArrayAdapter<AddLayersListItem> {
	private ArrayList<AddLayersListItem> items;
	
	public AddLayersListAdapter(Context context, int resource) {
		super(context, resource);
	}

	public AddLayersListAdapter(Context context, int resource, ArrayList<AddLayersListItem> items){
		super(context, resource, items);
		
		this.items = items;
	}
	
	/**
	 * @param position The index of the list item
	 * @param convertView A view that can be reused (For saving memory)
	 * @param parent 
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		if(view == null){
			LayoutInflater inflater = LayoutInflater.from(getContext());
			view = inflater.inflate(R.layout.add_layers_list_item, null);
		}
		
		AddLayersListItem listItem = items.get(position);
		
		if(listItem != null){
			TextView layerName = (TextView) view.findViewById(R.id.layerName);
			TextView serverName = (TextView) view.findViewById(R.id.serverName);
			CheckBox checkbox = (CheckBox) view.findViewById(R.id.add_layer_checkbox);
			
			if(layerName != null){
				layerName.setText(listItem.getLayerName());
			}
			
			if(serverName != null){
				serverName.setText(listItem.getServerName());
			}
			
			if(checkbox != null){
				checkbox.setChecked(listItem.isChecked());
				
				checkbox.setTag(Integer.valueOf(position));
				
				checkbox.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						Integer position = (Integer) ((CheckBox) v).getTag();
						AddLayersListItem listItem = items.get(position);
						listItem.setChecked(!listItem.isChecked());
					}
				});
			}
		}
		
		return view;
	}
}