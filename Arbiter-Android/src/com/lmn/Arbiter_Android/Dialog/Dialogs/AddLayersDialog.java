package com.lmn.Arbiter_Android.Dialog.Dialogs;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.AOIActivity;
import com.lmn.Arbiter_Android.Activities.ProjectsActivity;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.ListAdapters.AddLayersListAdapter;
import com.lmn.Arbiter_Android.ListAdapters.ServerListAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.AddLayersLoaderCallbacks;
import com.lmn.Arbiter_Android.LoaderCallbacks.ServerLoaderCallbacks;
import com.lmn.Arbiter_Android.Loaders.AddLayersListLoader;

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
	private Layer[] layersInProject;
	private boolean creatingProject;
	private ArbiterDialogs arbiterDialogs;
	
	public static AddLayersDialog newInstance(String title, String ok, 
			String cancel, int layout, Layer[] layersInProject){
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
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		
		this.getActivity().getSupportLoaderManager().destroyLoader(R.id.loader_servers_dropdown);
		this.getActivity().getSupportLoaderManager().destroyLoader(R.id.loader_add_layers);
	}
	
	@Override
	public void onPositiveClick() {
		// TODO: THIS IS GOING TO CHANGE...
		// If project is null, then we're adding a layer in project
		if(!creatingProject){
			// write the added layers to the database
			final Context context = getActivity().getApplicationContext();
			
			final ArrayList<Layer> list = new ArrayList<Layer>();
			ArrayList<Layer> checked = this.addLayersAdapter.getCheckedLayers();
			
			// Create a deep copy of the list of the checked layers
			for(int i = 0; i < checked.size(); i++){
				list.add(new Layer(checked.get(i)));
			}
			
			CommandExecutor.runProcess(new Runnable(){
				@Override
				public void run() {
					long projectId = ArbiterProject.getArbiterProject().getOpenProject(context);
					GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(context);
					LayersHelper.getLayersHelper().insert(helper.getWritableDatabase(), context, list, projectId);
				}
				
			});
			
		}else{
			// Add the layers to the ProjectListItem
			ArbiterProject.getArbiterProject().getNewProject().addLayers(this.addLayersAdapter.getCheckedLayers());
			Intent projectsIntent = new Intent(getActivity(), AOIActivity.class);
    		this.startActivity(projectsIntent);
		}
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
							getActivity().getSupportFragmentManager())).showAddServerDialog();
				}
			});
		}
		
		this.serverAdapter = new ServerListAdapter(this.getActivity().
				getApplicationContext(), R.layout.spinner_item, 
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
        this.serverLoaderCallbacks = new ServerLoaderCallbacks(this, this.serverAdapter, R.id.loader_servers_dropdown);
	}
	
	private void populateAddLayersList(View view){
		this.listView = (ListView) view.findViewById(R.id.addLayersListView);
		this.addLayersAdapter = new AddLayersListAdapter(this.getActivity().
				getApplicationContext(), R.layout.add_layers_list_item);
		this.listView.setAdapter(this.addLayersAdapter);
		
		this.addLayersLoaderCallbacks = new AddLayersLoaderCallbacks(this, this.addLayersAdapter, R.id.loader_add_layers);
	}
	
	public Spinner getSpinner(){
		return this.spinner;
	}
	
	public ServerListAdapter getAdapter(){
		return this.serverAdapter;
	}
	
	public Layer[] getLayersInProject(){
		return this.layersInProject;
	}
}
