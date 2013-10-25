package com.lmn.Arbiter_Android.ListAdapters;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ServersHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class ServerListAdapter extends BaseAdapter{

	private Server[] items;
	private final LayoutInflater inflater;
	private int itemLayout;
	private int textId;
	private int dropDownLayout;
	private final Context context;
	
	public ServerListAdapter(Context context, int itemLayout, 
			int textId){
		
			inflater = LayoutInflater.from(context);
			items = new Server[0];
			this.itemLayout = itemLayout;
			this.textId = textId;
			this.dropDownLayout = R.layout.drop_down_item;
			this.context = context;
	}
	
	public ServerListAdapter(Context context, int itemLayout, 
			int textId, Integer dropDownLayout){
		
			inflater = LayoutInflater.from(context);
			items = new Server[0];
			this.itemLayout = itemLayout;
			this.textId = textId;
			this.dropDownLayout = dropDownLayout;
			this.context = context;
	}
	
	public void setData(Server[] data){
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
		
		final Server server = items[position];
		
		if(server != null){
			TextView serverName = (TextView) view.findViewById(textId);
			
			if(serverName != null){
				serverName.setText(server.getServerName());
			}
			
			ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteServer);
			
			if(deleteButton != null){
            	deleteButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						CommandExecutor.runProcess(new Runnable(){
							@Override
							public void run() {
								
								GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(context);
								ServersHelper.getServersHelper().delete(helper.getWritableDatabase(), context, server);;
								
							}
							
						});
					}
            		
            	});
            }
		}
		
		return view;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		if(view == null){
			view = inflater.inflate(dropDownLayout, null);
		}
		
		Server listItem = items[position];
	
		if(listItem != null){
			TextView serverName = (TextView) view.findViewById(textId);
		
			if(serverName != null){
				serverName.setText(listItem.getServerName());
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
	public Server getItem(int position) {
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
