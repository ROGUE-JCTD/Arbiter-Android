package com.lmn.Arbiter_Android.Projects;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ProjectListAdapter extends ArrayAdapter<ProjectListItem> {

	private ArrayList<ProjectListItem> items;
	
	public ProjectListAdapter(Context context, int resource) {
		super(context, resource);
		// TODO Auto-generated constructor stub
	}
	
	public ProjectListAdapter(Context context, int resource, ArrayList<ProjectListItem> items){
		super(context, resource, items);
		
		this.items = items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		// Inflate the layout
		if(view == null){
			LayoutInflater inflater = LayoutInflater.from(getContext());
			view = inflater.inflate(R.layout.project_list_item, null);
		}
		
		ProjectListItem listItem = items.get(position);
		
		if(listItem != null){
			TextView layerName = (TextView) view.findViewById(R.id.projectName);
			
			if(layerName != null){
				layerName.setText(listItem.getProjectName());
			}
		}
		
		return view;
	}

}
