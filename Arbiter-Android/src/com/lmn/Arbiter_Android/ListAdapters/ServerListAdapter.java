package com.lmn.Arbiter_Android.ListAdapters;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ServersHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.GeometryEditor.GeometryEditor;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;

import android.app.AlertDialog;
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
	private boolean viewServerOnClickEnabled;
	
	public ServerListAdapter(FragmentActivity activity, int itemLayout, 
			int textId){
		
			inflater = LayoutInflater.from(activity.getApplicationContext());
			items = new SparseArray<Server>();
			this.itemLayout = itemLayout;
			this.textId = textId;
			this.dropDownLayout = R.layout.drop_down_item;
			this.activity = activity;
			this.viewServerOnClickEnabled = true;
			
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
			this.viewServerOnClickEnabled = false;
	}
	
	public void setData(SparseArray<Server> data){
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
		
		final Server server = getItem(position);
		
		if(server != null){
			TextView serverName = (TextView) view.findViewById(textId);
			
			if(serverName != null){
				serverName.setText(server.getName());
			}

			if(viewServerOnClickEnabled && !serverName.getText().equals("OpenStreetMap")){
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
					
						// Open the add server dialog
						(new ArbiterDialogs(activity.getApplicationContext(), activity.getResources(),
								activity.getSupportFragmentManager())).showAddServerDialog(server);
					}
				});
			}
			
			ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteServer);
			
			if(deleteButton != null){
				deleteButton.setEnabled(true);
				deleteButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						if(makeSureNotEditing()){
							displayDeletionAlert(server);
						}
					}
            		
            	});
            }
		}
		
		return view;
	}
	
	// Return true if not editing
    private boolean makeSureNotEditing(){
    		
		int editMode = mapChangeListener.getMapChangeHelper().getEditMode();
		
		if(editMode == GeometryEditor.Mode.OFF || editMode == GeometryEditor.Mode.SELECT){
			return true;
		}
			
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(R.string.finish_editing_title);
		builder.setMessage(R.string.finish_editing_message);
		builder.setIcon(R.drawable.icon);
		builder.setPositiveButton(android.R.string.ok, null);
		
		builder.create().show();
		
		return false;
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
							mapChangeListener.getMapChangeHelper()
								.onServerDeleted(server.getId());
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
