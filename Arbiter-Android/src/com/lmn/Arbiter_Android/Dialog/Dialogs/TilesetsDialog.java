package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.lmn.Arbiter_Android.Activities.AOIActivity;
import com.lmn.Arbiter_Android.Activities.HasThreadPool;
import com.lmn.Arbiter_Android.BaseClasses.BaseLayer;
import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.ConnectivityListeners.ConnectivityListener;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.Dialog.Dialogs.ChooseBaseLayer.ChooseBaselayerDialog;
import com.lmn.Arbiter_Android.ListAdapters.TilesetListAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.TilesetLoaderCallbacks;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesetsHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.R;

import java.util.ArrayList;

public class TilesetsDialog extends ArbiterDialogFragment{

	private TilesetListAdapter tilesetListAdapter;
	private ListView listView;
	private ConnectivityListener connectivityListener;
	private HasThreadPool hasThreadPool;
	private TilesetLoaderCallbacks tilesetLoaderCallbacks;
	private boolean newProject;
	
	public static TilesetsDialog newInstance(String title, String done, int layout, boolean newProject,
											 ConnectivityListener connectivityListener, HasThreadPool hasThreadPool){
		TilesetsDialog frag = new TilesetsDialog();
		
		frag.setTitle(title);
		frag.setOk(done);
		frag.setLayout(layout);
		frag.newProject = newProject;
		frag.connectivityListener = connectivityListener;
		frag.hasThreadPool = hasThreadPool;

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
		if (newProject){
			// Go to Base Layer
			Activity activity = getActivity();
			String title = activity.getResources().getString(R.string.choose_baselayer);
			String ok = activity.getResources().getString(android.R.string.ok);
			String cancel = activity.getResources().getString(android.R.string.cancel);

			ChooseBaselayerDialog newDialog = ChooseBaselayerDialog.newInstance(title, ok, cancel, R.layout.choose_baselayer_dialog,
					newProject, BaseLayer.createOSMBaseLayer(), connectivityListener, hasThreadPool);

			FragmentActivity fragActivity = (FragmentActivity)activity;
			newDialog.show(fragActivity.getSupportFragmentManager(), ChooseBaselayerDialog.TAG);
		}
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
							getActivity().getSupportFragmentManager())).showAddTilesetDialog(newProject, connectivityListener);
				}
				
			});
		}

		// Prepare TilesetHelper (restart any downloads)
		this.tilesetListAdapter.Init();
		TilesetsHelper.getTilesetsHelper().Init(getActivity());
		
		// Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        this.tilesetLoaderCallbacks = new TilesetLoaderCallbacks(this.getActivity(), this.tilesetListAdapter, R.id.loader_tilesets_list);
	}
}
