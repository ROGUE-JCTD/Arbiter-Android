package com.lmn.Arbiter_Android.Projects;

import com.lmn.Arbiter_Android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ProjectListAdapter extends BaseAdapter{
	private ProjectListItem[] items;
	private final LayoutInflater inflater;
	
	public ProjectListAdapter(Context context){
			inflater = LayoutInflater.from(context);
			items = new ProjectListItem[0];
	}
	
	public void setData(ProjectListItem[] data){
		items = data;
		
		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		// Inflate the layout
		if(view == null){
			view = inflater.inflate(R.layout.project_list_item, null);
		} 
		
		ProjectListItem listItem = items[position];
		
		if(listItem != null){
			TextView projectName = (TextView) view.findViewById(R.id.projectName);
			
			if(projectName != null){
				projectName.setText(listItem.getProjectName());
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
	public Object getItem(int position) {
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
