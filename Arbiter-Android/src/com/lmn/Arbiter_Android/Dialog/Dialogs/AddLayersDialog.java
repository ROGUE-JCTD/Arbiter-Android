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
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesetsHelper;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.Activities.HasThreadPool;
import com.lmn.Arbiter_Android.BaseClasses.BaseLayer;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.ConnectivityListeners.ConnectivityListener;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.Dialog.Dialogs.ChooseBaseLayer.ChooseBaselayerDialog;
import com.lmn.Arbiter_Android.ListAdapters.AddLayersListAdapter;
import com.lmn.Arbiter_Android.ListAdapters.ServerListAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.AddLayersLoaderCallbacks;
import com.lmn.Arbiter_Android.LoaderCallbacks.AddServerCallbacks;
import com.lmn.Arbiter_Android.LoaderCallbacks.ServerLoaderCallbacks;
import com.lmn.Arbiter_Android.Loaders.AddLayersListLoader;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class AddLayersDialog extends ArbiterDialogFragment{
	@SuppressWarnings("unused")
	private ServerLoaderCallbacks serverLoaderCallbacks;
	@SuppressWarnings("unused")
	private AddLayersLoaderCallbacks addLayersLoaderCallbacks;
	
	private ListView listView;
	private ServerListAdapter serverAdapter;
	private AddLayersListAdapter addLayersAdapter;
	private Spinner spinner;
	private ArrayList<Layer> layersInProject = null;
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
	
	public static AddLayersDialog newInstance(String title, String ok, 
			String cancel, int layout, ArrayList<Layer> layersInProject,
			ConnectivityListener connectivityListener, HasThreadPool hasThreadPool){
		
		AddLayersDialog frag = new AddLayersDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		frag.onCreateAlreadyFired = false;
		
		frag.layersInProject = layersInProject;
		frag.arbiterProject = ArbiterProject.getArbiterProject();
		frag.hasThreadPool = hasThreadPool;
		frag.connectivityListener = connectivityListener;
		
		return frag;
	}

	public static AddLayersDialog newInstance(String title, String ok, 
			String cancel, int layout, boolean creatingProject, 
			ConnectivityListener connectivityListener){
		
		AddLayersDialog frag = new AddLayersDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		frag.creatingProject = creatingProject;
		frag.layersInProject = null;
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
		this.getActivity().getSupportLoaderManager().destroyLoader(R.id.loader_add_layers);
	}
	
	@Override
	public void onPositiveClick() {
		
		final Context context = getActivity().getApplicationContext();
		
		final ArrayList<Layer> layers = new ArrayList<Layer>();
		ArrayList<Layer> checked = this.addLayersAdapter.getCheckedLayers();
		
		int highestColorIndex = -1;
		if(layersInProject != null) {
			for(int i = 0; i < layersInProject.size(); i++) {
				String layerColor = layersInProject.get(i).getColor();
				if(layerColor != null) {
					for(int j = 0; j < colors.length; j++) {
						if(layerColor.equals(colors[j])) {
							if(j > highestColorIndex) {
								highestColorIndex = j;
							}
							break;
						}
					}
				}
			}
		}
		
		// Create a deep copy of the list of the checked layers
		for(int i = 0; i < checked.size(); i++){
			highestColorIndex++;
			Layer layer = new Layer(checked.get(i));
			layer.setColor(colors[highestColorIndex%colors.length]);
			layers.add(layer);
		}
		
		if(!creatingProject){
			
			final String projectName = arbiterProject.getOpenProject(getActivity());
			
			// write the added layers to the database
			CommandExecutor.runProcess(new Runnable(){
				@Override
				public void run() {
					ProjectDatabaseHelper helper = ProjectDatabaseHelper
							.getHelper(context, ProjectStructure
									.getProjectPath(projectName), false);
					
					final long[] layerIds = LayersHelper.getLayersHelper().
								insert(helper.getWritableDatabase(), context, layers);
					
					getActivity().runOnUiThread(new Runnable(){
						@Override
						public void run(){
							
							mapChangeListener.getMapChangeHelper().onLayersAdded(layers, 
									layerIds, hasThreadPool);
							
							dismiss();
						}
					});
				}
				
			});
			
		}else{
			// Add the layers to the ProjectListItem
			Project newProject = ArbiterProject.getArbiterProject().getNewProject();
			newProject.addLayers(layers);
			
			//Intent projectsIntent = new Intent(getActivity(), AOIActivity.class);
    		//this.startActivity(projectsIntent);

			TilesetsHelper.getTilesetsHelper().newProjectTilesetsDialog(getActivity(), creatingProject,
					connectivityListener, hasThreadPool);
			
			dismiss();
		}
	}
	
	@Override
	public void onNegativeClick() {
		if(creatingProject){
			Log.w("AddLayersDialog", "AddLayersDialog dismissed!");
			ArbiterProject.getArbiterProject().doneCreatingProject(getActivity().getApplicationContext());
		}
	}

	@Override
	public void beforeCreateDialog(View view) {
		if(view != null){
			registerListeners(view);
			populateAddLayersList(view);
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
				Log.w("AddLayersDialog", "AddLayersDialog spinner updated");
				
				if(!onCreateAlreadyFired){
					onCreateAlreadyFired = true;
				}else{
					// Server was selected so force the AddLayersListLoader to load
					LocalBroadcastManager.getInstance(context).
						sendBroadcast(new Intent(AddLayersListLoader.ADD_LAYERS_LIST_UPDATED));
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
	
	private void populateAddLayersList(View view){
		this.listView = (ListView) view.findViewById(R.id.addLayersListView);
		this.addLayersAdapter = new AddLayersListAdapter
				(this.getActivity().getApplicationContext(), R.layout.add_layers_list_item);
		this.listView.setAdapter(this.addLayersAdapter);
		
		this.addLayersLoaderCallbacks = new AddLayersLoaderCallbacks(this, this.addLayersAdapter, R.id.loader_add_layers);
	}
	
	public Spinner getSpinner(){
		return this.spinner;
	}
	
	public ServerListAdapter getAdapter(){
		return this.serverAdapter;
	}
	
	public ArrayList<Layer> getLayersInProject(){
		return this.layersInProject;
	}
}
