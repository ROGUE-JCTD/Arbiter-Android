package com.lmn.Arbiter_Android.ListAdapters;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ServersHelper;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class ServerListAdapter extends BaseAdapter{

	private ArrayList<Server> items;
	private final LayoutInflater inflater;
	private int itemLayout;
	private int textId;
	private int dropDownLayout;
	private final Activity activity;
	
	public ServerListAdapter(Activity activity, int itemLayout, 
			int textId){
		
			inflater = LayoutInflater.from(activity.getApplicationContext());
			items = new ArrayList<Server>();
			this.itemLayout = itemLayout;
			this.textId = textId;
			this.dropDownLayout = R.layout.drop_down_item;
			this.activity = activity;
	}
	
	public ServerListAdapter(Activity activity, int itemLayout, 
			int textId, Integer dropDownLayout){
		
			inflater = LayoutInflater.from(activity.getApplicationContext());
			items = new ArrayList<Server>();
			this.itemLayout = itemLayout;
			this.textId = textId;
			this.dropDownLayout = dropDownLayout;
			this.activity = activity;
			
	}
	
	private void addDefaultServer(ArrayList<Server> servers){
		if(servers != null){
			if(!ArbiterProject.getArbiterProject().includeDefaultLayer()){
				servers.add(new Server(Server.DEFAULT_SERVER_NAME, null, 
						null, null, Server.DEFAULT_FLAG));
			}
		}
	}
	
	public void setData(ArrayList<Server> data){
		items = data;
		
		addDefaultServer(items);
		
		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		// Inflate the layout
		if(view == null){
			view = inflater.inflate(itemLayout, null);
		}
		
		final Server server = getItem(position);
		
		if(server != null){
			TextView serverName = (TextView) view.findViewById(textId);
			
			if(serverName != null){
				serverName.setText(server.getServerName());
			}
			
			ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteServer);
			
			if(deleteButton != null){
				if(Server.isDefaultServer(server.getId())){
					deleteButton.setEnabled(false);
				}else{
					deleteButton.setEnabled(true);
					deleteButton.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							displayDeletionAlert(server);
						}
	            		
	            	});
				}
            }
		}
		
		return view;
	}
	
	private void displayDeletionAlert(final Server server){
		ServersHelper.getServersHelper().deletionAlert(activity, new Runnable(){

			@Override
			public void run() {
				CommandExecutor.runProcess(new Runnable(){
					@Override
					public void run() {
						
						GlobalDatabaseHelper helper = GlobalDatabaseHelper.
								getGlobalHelper(activity.getApplicationContext());
						ServersHelper.getServersHelper().delete(helper.getWritableDatabase(),
								activity.getApplicationContext(), server);
					}
					
				});
			}
		});
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		if(view == null){
			view = inflater.inflate(dropDownLayout, null);
		}
		
		Server listItem = getItem(position);
	
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
		
		return items.size();
	}

	@Override
	public Server getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
