package com.lmn.Arbiter_Android.Dialog.Dialogs;


import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.lmn.Arbiter_Android.ConnectivityListeners.ConnectivityListener;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.ListAdapters.TilesetListAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.TilesetLoaderCallbacks;
import com.lmn.Arbiter_Android.R;

public class TilesetsDialog extends ArbiterDialogFragment{

	private TilesetListAdapter tilesetListAdapter;
	private ListView listView;

	private ConnectivityListener connectivityListener;

	//@SuppressWarnings("unused")

	private TilesetLoaderCallbacks tilesetLoaderCallbacks;
	
	public static TilesetsDialog newInstance(String title, String ok,
			String cancel, int layout){
		TilesetsDialog frag = new TilesetsDialog();
		
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
		
		// Initialize the list of tilesets
		this.listView = (ListView) view.findViewById(R.id.tilesetListView);
		this.tilesetListAdapter = new TilesetListAdapter(this.getActivity(), R.layout.tileset_list_item, R.id.tilesetName);
		this.listView.setAdapter(this.tilesetListAdapter);
		this.connectivityListener = new ConnectivityListener(this.getActivity());

		ImageButton button = (ImageButton) view.findViewById(R.id.add_tileset_button);
		
		if(button != null){
			button.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view) {
					// Open the add server dialog
					(new ArbiterDialogs(getActivity().getApplicationContext(), getActivity().getResources(),
							getActivity().getSupportFragmentManager())).showAddTilesetDialog(null, connectivityListener);
				}
				
			});
		}
		
		// Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        this.tilesetLoaderCallbacks = new TilesetLoaderCallbacks(this.getActivity(), this.tilesetListAdapter, R.id.loader_tilesets_list);
	}
}
