package com.lmn.Arbiter_Android.Dialog.Dialogs;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import com.lmn.Arbiter_Android.Activities.HasThreadPool;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.ConnectivityListeners.ConnectivityListener;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.ListAdapters.AddTilesetsListAdapter;
import com.lmn.Arbiter_Android.ListAdapters.ServerListAdapter;
import com.lmn.Arbiter_Android.ListAdapters.TilesetListAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.AddTilesetsLoaderCallbacks;
import com.lmn.Arbiter_Android.LoaderCallbacks.AddServerCallbacks;
import com.lmn.Arbiter_Android.LoaderCallbacks.ServerLoaderCallbacks;
import com.lmn.Arbiter_Android.Loaders.AddTilesetsListLoader;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class AddTilesetDialog extends ArbiterDialogFragment{
	@SuppressWarnings("unused")
	private ServerLoaderCallbacks serverLoaderCallbacks;
	@SuppressWarnings("unused")
	private AddTilesetsLoaderCallbacks addTilesetsLoaderCallbacks;

	private ListView listView;
	private ServerListAdapter serverAdapter;
	private AddTilesetsListAdapter addTilesetAdapter;
	private Spinner spinner;
	private ArrayList<Tileset> tilesetsInProject = null;
	private boolean creatingProject;
	private boolean onCreateAlreadyFired;
	private ConnectivityListener connectivityListener;
	private HasThreadPool hasThreadPool;
	private MapChangeListener mapChangeListener;
	private ArbiterProject arbiterProject;

	private String[] colors = {"teal", "maroon", "green",
			"purple", "fuchsia", "lime",
			"red", "black", "navy",
			"aqua", "grey", "olive",
			"yellow", "silver", "white"};

	public static AddTilesetDialog newInstance(String title, String ok,
											  String cancel, int layout, ArrayList<Tileset> tilesetsInProject,
											  ConnectivityListener connectivityListener){// HasThreadPool hasThreadPool){

		AddTilesetDialog frag = new AddTilesetDialog();

		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);

		frag.onCreateAlreadyFired = false;

		frag.tilesetsInProject = tilesetsInProject;
		frag.arbiterProject = ArbiterProject.getArbiterProject();

		//frag.hasThreadPool = hasThreadPool;
		frag.connectivityListener = connectivityListener;

		return frag;
	}

	public static AddTilesetDialog newInstance(String title, String ok,
											  String cancel, int layout, boolean creatingProject,
											  ConnectivityListener connectivityListener){

		AddTilesetDialog frag = new AddTilesetDialog();

		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		frag.creatingProject = creatingProject;
		frag.tilesetsInProject = null;
		frag.connectivityListener = connectivityListener;

		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);

		this.setValidatingClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {

				if(connectivityListener != null && connectivityListener.isConnected()){
					onPositiveClick();
				}else{
					Util.showNoNetworkDialog(getActivity());
				}
			}
		});

		if(!creatingProject){
			try {
				mapChangeListener = (MapChangeListener) getActivity();
			} catch (ClassCastException e){
				e.printStackTrace();
				throw new ClassCastException(getActivity().toString()
						+ " must implement MapChangeListener");
			}
		}
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

		final Context context = getActivity().getApplicationContext();

		final ArrayList<Tileset> tilesets = new ArrayList<Tileset>();
		ArrayList<Tileset> checked = this.addTilesetAdapter.getCheckedTilesets();

//		int highestColorIndex = -1;
//		if(tilesetsInProject != null) {
//			for(int i = 0; i < tilesetsInProject.size(); i++) {
//				String tileColor = tilesetsInProject.get(i).getColor();
//				if(tileColor != null) {
//					for(int j = 0; j < colors.length; j++) {
//						if(tileColor.equals(colors[j])) {
//							if(j > highestColorIndex) {
//								highestColorIndex = j;
//							}
//							break;
//						}
//					}
//				}
//			}
//		}

		// Create a deep copy of the list of the checked layers
		for(int i = 0; i < checked.size(); i++){
			//highestColorIndex++;
			Tileset tileset = new Tileset(checked.get(i));
			//layer.setColor(colors[highestColorIndex%colors.length]);
			tilesets.add(tileset);
		}

		if(!creatingProject){

			final String projectName = arbiterProject.getOpenProject(getActivity());

			// TODO: write the added tilesets to the APPLICATION database
			CommandExecutor.runProcess(new Runnable(){
				@Override
				public void run() {
					ApplicationDatabaseHelper helper = ApplicationDatabaseHelper
							.getHelper(context);

					//final long[] tilesetIds = LayersHelper.getLayersHelper().
				//			insert(helper.getWritableDatabase(), context, tilesets);

					getActivity().runOnUiThread(new Runnable(){
						@Override
						public void run(){

							//mapChangeListener.getMapChangeHelper().onLayersAdded(layers,
						//			layerIds, hasThreadPool);

							dismiss();
						}
					});
				}

			});

		}else{
			// Add the tilesets to the ProjectListItem
			//Project newProject = ArbiterProject.getArbiterProject().getNewProject();
			//newProject.addTilesets(tilesets);

			//Intent projectsIntent = new Intent(getActivity(), AOIActivity.class);
			//this.startActivity(projectsIntent);

			//FragmentActivity activity = getActivity();

			//String title = activity.getResources().getString(R.string.choose_baselayer);
			//String ok = activity.getResources().getString(android.R.string.ok);
			//String cancel = activity.getResources().getString(android.R.string.cancel);

			//ChooseBaselayerDialog dialog = ChooseBaselayerDialog.newInstance(title, ok, cancel, R.layout.choose_baselayer_dialog,
			//		creatingProject, BaseLayer.createOSMBaseLayer(), connectivityListener, hasThreadPool);

			//dialog.show(activity.getSupportFragmentManager(), ChooseBaselayerDialog.TAG);

			dismiss();
		}
	}

	@Override
	public void onNegativeClick() {
		if(creatingProject){
			Log.w("AddTilesetsDialog", "AddTilesetsDialog dismissed!");
			ArbiterProject.getArbiterProject().doneCreatingProject(getActivity().getApplicationContext());
		}
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
		}

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

					//Log.w("BIGBANGBOOM", "ITS TIME TO PARTY");
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

	/**
	 * Get the selected server from the dropdown
	 * @return The selected server
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
		this.addTilesetAdapter = new AddTilesetsListAdapter
				(this.getActivity().getApplicationContext(), R.layout.add_tilesets_list_item);
		this.listView.setAdapter(this.addTilesetAdapter);

		this.addTilesetsLoaderCallbacks = new AddTilesetsLoaderCallbacks(this, this.addTilesetAdapter, R.id.loader_add_tilesets);
	}

	public Spinner getSpinner(){
		return this.spinner;
	}

	public ServerListAdapter getAdapter(){
		return this.serverAdapter;
	}

	public ArrayList<Tileset> getTilesetsInProject(){
		return this.tilesetsInProject;
	}
}
