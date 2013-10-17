package com.lmn.Arbiter_Android.Dialog.Dialogs;


import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.ListAdapters.ServerListAdapter;
import com.lmn.Arbiter_Android.ListItems.ServerListItem;
import com.lmn.Arbiter_Android.Loaders.ServersListLoader;

public class ServersDialog extends ArbiterDialogFragment implements LoaderManager.LoaderCallbacks<ServerListItem[]>{
	private ServerListAdapter serverAdapter;
	private ListView listView;
	
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
		this.serverAdapter = new ServerListAdapter(this.getActivity().
				getApplicationContext(), R.layout.server_list_item, R.id.serverName, null);
		this.listView.setAdapter(this.serverAdapter);
		    
		ImageButton button = (ImageButton) view.findViewById(R.id.add_server_button);
		
		if(button != null){
			button.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view) {
					// Open the add server dialog
					(new ArbiterDialogs(getActivity().getResources(),
							getActivity().getSupportFragmentManager())).showAddServerDialog();
				}
				
			});
		}
		
		// Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        this.getActivity().getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<ServerListItem[]> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new ServersListLoader(this.getActivity().getApplicationContext());
	}

	@Override
	public void onLoadFinished(Loader<ServerListItem[]> loader, ServerListItem[] data) {
		serverAdapter.setData(data);
	}

	@Override
	public void onLoaderReset(Loader<ServerListItem[]> loader) {
		serverAdapter.setData(null);
	}	
}
