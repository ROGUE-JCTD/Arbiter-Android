package com.lmn.Arbiter_Android.Dialog.Dialogs;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.AOIActivity;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.ListAdapters.AddLayersListAdapter;
import com.lmn.Arbiter_Android.ListAdapters.ServerListAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.AddLayersLoaderCallbacks;
import com.lmn.Arbiter_Android.LoaderCallbacks.ServerLoaderCallbacks;
import com.lmn.Arbiter_Android.Loaders.AddLayersListLoader;
import com.lmn.Arbiter_Android.Loaders.LayersListLoader;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;

import android.content.ContentValues;
import android.content.Context;
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

public class AddLayersDialog extends ArbiterDialogFragment{
	@SuppressWarnings("unused")
	private ServerLoaderCallbacks serverLoaderCallbacks;
	@SuppressWarnings("unused")
	private AddLayersLoaderCallbacks addLayersLoaderCallbacks;
	
	private ListView listView;
	private ServerListAdapter serverAdapter;
	private AddLayersListAdapter addLayersAdapter;
	private Spinner spinner;
	private ArrayList<Layer> layersInProject;
	private boolean creatingProject;
	
	private MapChangeListener mapChangeListener;
	
	public static AddLayersDialog newInstance(String title, String ok, 
			String cancel, int layout, ArrayList<Layer> layersInProject){
		AddLayersDialog frag = new AddLayersDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		frag.layersInProject = layersInProject;
		
		return frag;
	}

	public static AddLayersDialog newInstance(String title, String ok, 
			String cancel, int layout, boolean creatingProject){
		AddLayersDialog frag = new AddLayersDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		frag.creatingProject = creatingProject;
		frag.layersInProject = null;
		
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		
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
		
		// Create a deep copy of the list of the checked layers
		for(int i = 0; i < checked.size(); i++){
			layers.add(new Layer(checked.get(i)));
		}
		
		final boolean includeDefaultLayer = this.addLayersAdapter.includeDefaultLayer();
		final long projectId = ArbiterProject.getArbiterProject().getOpenProject(context);
		
		if(!creatingProject){
			// write the added layers to the database
			CommandExecutor.runProcess(new Runnable(){
				@Override
				public void run() {
					GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(context);
					long[] layerIds = LayersHelper.getLayersHelper().
								insert(helper.getWritableDatabase(), context, layers, projectId);
					
					if(includeDefaultLayer){
						setDefaultLayerInfo(projectId, includeDefaultLayer);
					}
					
					mapChangeListener.onLayersAdded(layers, layerIds, includeDefaultLayer, true);
				}
				
			});
			
		}else{
			// Add the layers to the ProjectListItem
			Project newProject = ArbiterProject.getArbiterProject().getNewProject();
			newProject.addLayers(layers);
			newProject.includeDefaultLayer(includeDefaultLayer);
			newProject.setDefaultLayerVisibility(includeDefaultLayer);
			
			Intent projectsIntent = new Intent(getActivity(), AOIActivity.class);
    		this.startActivity(projectsIntent);
		}
	}
	
	private void setDefaultLayerInfo(final long projectId, final boolean includeDefaultLayer){
		final Context context = getActivity().getApplicationContext();
		
		ContentValues values = new ContentValues();
		values.put(ProjectsHelper.INCLUDE_DEFAULT_LAYER, true);
		values.put(ProjectsHelper.DEFAULT_LAYER_VISIBILITY, true);
		
		ArbiterProject.getArbiterProject().updateAttributeValues(context, projectId, values, new Runnable(){
			@Override
			public void run(){
				LocalBroadcastManager.getInstance(context).
				sendBroadcast(new Intent(LayersListLoader.LAYERS_LIST_UPDATED));
			}
		});
	}
	
	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
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
					(new ArbiterDialogs(getActivity().getResources(), 
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
		        // Server was selected so force the AddLayersListLoader to load
				LocalBroadcastManager.getInstance(context).
					sendBroadcast(new Intent(AddLayersListLoader.ADD_LAYERS_LIST_UPDATED));
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        // TODO
		    }
		});
		
		this.spinner.setAdapter(this.serverAdapter);
		
		// Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        this.serverLoaderCallbacks = new ServerLoaderCallbacks(this, 
        		this.serverAdapter, R.id.loader_servers_dropdown);
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
