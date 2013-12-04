package com.lmn.Arbiter_Android.ListAdapters;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.ArbiterState;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ServersHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class ServerListAdapter extends BaseAdapter implements ArbiterAdapter<SparseArray<Server>>{
	private MapChangeListener mapChangeListener;
	
	private SparseArray<Server> items;
	private final LayoutInflater inflater;
	private int itemLayout;
	private int textId;
	private int dropDownLayout;
	private final FragmentActivity activity;
	
	public ServerListAdapter(FragmentActivity activity, int itemLayout, 
			int textId){
		
			inflater = LayoutInflater.from(activity.getApplicationContext());
			items = new SparseArray<Server>();
			this.itemLayout = itemLayout;
			this.textId = textId;
			this.dropDownLayout = R.layout.drop_down_item;
			this.activity = activity;
			
			try {
				mapChangeListener = (MapChangeListener) activity;
			} catch (ClassCastException e){
				throw new ClassCastException(activity.toString() 
						+ " must implement MapChangeListener");
			}
	}
	
	public ServerListAdapter(FragmentActivity activity, int itemLayout, 
			int textId, Integer dropDownLayout){
		
			inflater = LayoutInflater.from(activity.getApplicationContext());
			items = new SparseArray<Server>();
			this.itemLayout = itemLayout;
			this.textId = textId;
			this.dropDownLayout = dropDownLayout;
			this.activity = activity;
	}
	
	private void addDefaultServer(SparseArray<Server> servers){
		if(servers != null){
			ArbiterProject arbiterProject = ArbiterProject.getArbiterProject();
			
			if(((arbiterProject.includeDefaultLayer() != null) && 
					!arbiterProject.includeDefaultLayer().equals("true")) 
					|| (ArbiterState.getArbiterState().isCreatingProject())){
				servers.put(Server.DEFAULT_FLAG, new Server(Server.DEFAULT_SERVER_NAME, null, 
						null, null, Server.DEFAULT_FLAG));
			}
		}
	}
	
	public void setData(SparseArray<Server> data){
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
				serverName.setText(server.getName());
			}
			
			view.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					// Open the add server dialog
					(new ArbiterDialogs(activity.getApplicationContext(), activity.getResources(),
							activity.getSupportFragmentManager())).showAddServerDialog(server);
				}
			});
			
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
		final Context context = activity.getApplicationContext();
		
		ServersHelper.getServersHelper().deletionAlert(activity, new Runnable(){

			@Override
			public void run() {
				final String deletingServerTitle = context.getResources().getString(R.string.deleting_server);
				final String deletingServerMsg = context.getResources().getString(R.string.deleting_server_msg);
				
				final ProgressDialog dialog = ProgressDialog.show(activity, 
						deletingServerTitle, deletingServerMsg, true);
				
				CommandExecutor.runProcess(new Runnable(){
					@Override
					public void run() {
						
						ServersHelper.getServersHelper().delete(
								activity, server);
						
						if(mapChangeListener != null){
							mapChangeListener.onServerDeleted(server.getId());
						}
						
						dialog.dismiss();
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
				serverName.setText(listItem.getName());
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
		return items.valueAt(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
