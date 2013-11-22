package com.lmn.Arbiter_Android.Dialog.Dialogs;


import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.ListAdapters.ServerListAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.ServerLoaderCallbacks;

public class ServersDialog extends ArbiterDialogFragment{
	private ServerListAdapter serverAdapter;
	private ListView listView;
	@SuppressWarnings("unused")
	private ServerLoaderCallbacks serverLoaderCallbacks;
	
	public static ServersDialog newInstance(String title, String ok, 
			String cancel, int layout){
		ServersDialog frag = new ServersDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		this.getActivity().getSupportLoaderManager().destroyLoader(R.id.loader_servers_list);
	}
	
	@Override
	public void onPositiveClick() {
		
	}

	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCreateDialog(View view) {
		
		// Initialize the list of servers
		this.listView = (ListView) view.findViewById(R.id.serverListView);
		this.serverAdapter = new ServerListAdapter(this.getActivity(), R.layout.server_list_item, R.id.serverName);
		this.listView.setAdapter(this.serverAdapter);
		    
		ImageButton button = (ImageButton) view.findViewById(R.id.add_server_button);
		
		if(button != null){
			button.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view) {
					// Open the add server dialog
					(new ArbiterDialogs(getActivity().getApplicationContext(), getActivity().getResources(),
							getActivity().getSupportFragmentManager())).showAddServerDialog(null);
				}
				
			});
		}
		
		// Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        this.serverLoaderCallbacks = new ServerLoaderCallbacks(this, this.serverAdapter, R.id.loader_servers_list);
	}
}
