package com.lmn.Arbiter_Android.Dialog.Dialogs;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.ConnectivityListeners.ConnectivityListener;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.ListAdapters.AddTilesetsListAdapter;
import com.lmn.Arbiter_Android.ListAdapters.ServerListAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.AddTilesetsLoaderCallbacks;
import com.lmn.Arbiter_Android.LoaderCallbacks.AddServerCallbacks;
import com.lmn.Arbiter_Android.LoaderCallbacks.ServerLoaderCallbacks;
import com.lmn.Arbiter_Android.Loaders.AddTilesetsListLoader;

public class AddTilesetDialog extends ArbiterDialogFragment{
	@SuppressWarnings("unused")
	private ServerLoaderCallbacks serverLoaderCallbacks;
	@SuppressWarnings("unused")
	private AddTilesetsLoaderCallbacks addTilesetsLoaderCallbacks;

	private ListView listView;
	private ServerListAdapter serverAdapter;
	private AddTilesetsListAdapter addTilesetAdapter;
	private Spinner spinner;
	private boolean creatingProject;
	private boolean onCreateAlreadyFired;
	private ConnectivityListener connectivityListener;

	public static AddTilesetDialog newInstance(String title, String done, int layout,
											  ConnectivityListener connectivityListener, boolean newProject){

		AddTilesetDialog frag = new AddTilesetDialog();

		frag.setTitle(title);
		frag.setCancel(done);
		frag.setLayout(layout);

		frag.creatingProject = newProject;
		frag.onCreateAlreadyFired = false;
		frag.connectivityListener = connectivityListener;

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);

		this.setValidatingClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (connectivityListener != null && connectivityListener.isConnected()) {
					onPositiveClick();
				} else {
					Util.showNoNetworkDialog(getActivity());
				}
			}
		});
	}

	@Override
	public void onCancel(DialogInterface dialog){
		onNegativeClick();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();

		this.getActivity().getSupportLoaderManager().destroyLoader(R.id.loader_servers_dropdown);
		this.getActivity().getSupportLoaderManager().destroyLoader(R.id.loader_add_tilesets);
	}

	@Override
	public void onPositiveClick() {

	}

	@Override
	public void onNegativeClick() {

	}

	@Override
	public void beforeCreateDialog(View view) {
		if(view != null){
			registerListeners(view);
			populateAddTilesetsList(view);
		}
	}

	private void registerListeners(View view){

		ImageButton button = (ImageButton) view.findViewById(R.id.add_server_button);

		if(button != null){
			button.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					(new ArbiterDialogs(getActivity().getApplicationContext(), getActivity().getResources(),
							getActivity().getSupportFragmentManager())).showAddServerDialog(null);
				}
			});

			this.serverAdapter = new ServerListAdapter(this.getActivity(), R.layout.spinner_item,
					R.id.spinnerText, R.layout.drop_down_item);

			this.spinner = (Spinner) view.findViewById(R.id.serversSpinner);

			final Context context = this.getActivity().getApplicationContext();

			this.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					Log.w("AddTilesetDialog", "AddTilesetDialog spinner updated");

					if(!onCreateAlreadyFired){
						onCreateAlreadyFired = true;
					}else{

						// Server was selected so force the AddTilesetsListLoader to load
						LocalBroadcastManager.getInstance(context).
								sendBroadcast(new Intent(AddTilesetsListLoader.ADD_TILESETS_LIST_UPDATED));
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
					// TODO
				}
			});

			this.spinner.setAdapter(this.serverAdapter);

			// Prepare the loader.  Either re-connect with an existing one,
			// or start a new one.
			this.serverLoaderCallbacks = new AddServerCallbacks(this,
					this.serverAdapter, R.id.loader_servers_dropdown, this.spinner);
		}
	}

	/**
	 * Get the selected server from the dropdown
	 * @return The selected server
	 *
	 */
	public Server getSelectedServer(){
		int selectedIndex = getSpinner().getSelectedItemPosition();

		if(selectedIndex > -1)
			return getAdapter().getItem(selectedIndex);
		else
			return null;
	}

	private void populateAddTilesetsList(View view){
		this.listView = (ListView) view.findViewById(R.id.addTilesetsListView);
		this.addTilesetAdapter = new AddTilesetsListAdapter(this.getActivity(), R.layout.add_tilesets_list_item);
		this.listView.setAdapter(this.addTilesetAdapter);

		this.addTilesetsLoaderCallbacks = new AddTilesetsLoaderCallbacks(this, this.addTilesetAdapter, R.id.loader_add_tilesets);
	}

	public Spinner getSpinner(){
		return this.spinner;
	}

	public ServerListAdapter getAdapter(){
		return this.serverAdapter;
	}

}
