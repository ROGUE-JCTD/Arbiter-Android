package com.lmn.Arbiter_Android.ListAdapters;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ListItems.AddLayersListItem;
import com.lmn.Arbiter_Android.Projects.ProjectComponents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class AddLayersListAdapter extends BaseAdapter {
	private ArrayList<AddLayersListItem> items;
	private LayoutInflater inflater;
	private int itemLayout;
	
	public AddLayersListAdapter(Context context, int itemLayout) {
		inflater = LayoutInflater.from(context);
		this.items = new ArrayList<AddLayersListItem>();
		this.itemLayout = itemLayout;
	}
	
	public void setData(ArrayList<AddLayersListItem> items){
		this.items = items;
		this.notifyDataSetChanged();
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
			view = inflater.inflate(itemLayout, null);
		}
		
		AddLayersListItem listItem = items.get(position);
		
		if(listItem != null){
			TextView layerName = (TextView) view.findViewById(R.id.layerName);
			TextView serverName = (TextView) view.findViewById(R.id.serverName);
			CheckBox checkbox = (CheckBox) view.findViewById(R.id.addLayerCheckbox);
			
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
						boolean checked = !listItem.isChecked();
						
						listItem.setChecked(checked);
						ProjectComponents project = ProjectComponents.getProjectComponents();
						
						if(checked){
							project.addLayer(listItem);
						}else{
							project.removeLayer(listItem);
						}
					}
				});
			}
		}
		
		return view;
	}

	@Override
	public int getCount() {
		return this.items.size();
	}

	@Override
	public Object getItem(int position) {
		return this.items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}