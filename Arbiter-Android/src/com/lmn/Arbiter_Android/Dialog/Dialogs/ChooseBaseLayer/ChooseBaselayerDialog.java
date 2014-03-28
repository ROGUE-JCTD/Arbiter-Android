package com.lmn.Arbiter_Android.Dialog.Dialogs.ChooseBaseLayer;

import java.util.ArrayList;

import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.AOIActivity;
import com.lmn.Arbiter_Android.BaseClasses.BaseLayer;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.ListAdapters.ChooseBaseLayerAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.ChooseBaseLayerLoaderCallbacks;
import com.lmn.Arbiter_Android.Loaders.BaseLayerLoader;
import com.lmn.Arbiter_Android.Loaders.LayersListLoader;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class ChooseBaselayerDialog extends ArbiterDialogFragment implements BaseLayerUpdater{
	public static final String TAG = "ChooseBaselayerDialog";
	
	@SuppressWarnings("unused")
	private ChooseBaseLayerLoaderCallbacks layerLoaderCallbacks;
	
	private ListView listView;
	private ChooseBaseLayerAdapter layersAdapter;
	private boolean creatingProject;
	private ArbiterProject arbiterProject;
	private TextView selectedBaseLayerField;
	private BaseLayer baseLayer;
	private BaseLayer selectedBaseLayer;
	
	public static ChooseBaselayerDialog newInstance(String title, String ok, 
			String cancel, int layout, boolean creatingProject, BaseLayer selectedBaseLayer){
		ChooseBaselayerDialog frag = new ChooseBaselayerDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		frag.creatingProject = creatingProject;
		frag.selectedBaseLayerField = null;
		frag.baseLayer = null;
		frag.arbiterProject = ArbiterProject.getArbiterProject();
		frag.selectedBaseLayer = selectedBaseLayer;
		
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}

	@Override
	public void onCancel(DialogInterface dialog){
		onNegativeClick();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		if(!creatingProject){
			this.getActivity().getSupportLoaderManager().destroyLoader(R.id.loader_layers_choose_baselayer);
		}
	}
	
	@Override
	public void onPositiveClick() {
		final Activity activity = getActivity();
		
		final Context context = activity.getApplicationContext();
		
		if(!creatingProject){
			
			if(baseLayer == null){
				return;
			}
			
			final String projectName = arbiterProject.getOpenProject(activity);
			
			String title = context.getResources().getString(R.string.loading);
			String message = context.getResources().getString(R.string.please_wait);
			
			final ProgressDialog progressDialog = ProgressDialog.show(activity, title, message);
			
			// write the added layers to the database
			CommandExecutor.runProcess(new Runnable(){
				@Override
				public void run() {
					ProjectDatabaseHelper helper = ProjectDatabaseHelper
							.getHelper(context, ProjectStructure
									.getProjectPath(projectName), false);
					
					try {
						PreferencesHelper.getHelper().put(helper.getWritableDatabase(),
								context, PreferencesHelper.BASE_LAYER, "[" + baseLayer.getJSON().toString() + "]");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					activity.runOnUiThread(new Runnable(){
						@Override
						public void run(){
							
							LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(BaseLayerLoader.BASE_LAYER_CHANGED));
							LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(LayersListLoader.LAYERS_LIST_UPDATED));
							progressDialog.dismiss();
						}
					});
				}
			});
			
		}else{
			Project newProject = ArbiterProject.getArbiterProject().getNewProject();
			
			// Set the base layer for the new project
			newProject.setBaseLayer(baseLayer);
			
			Intent projectsIntent = new Intent(getActivity(), AOIActivity.class);
    		this.startActivity(projectsIntent);
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
			populateAddLayersList(view);
		}
	}
	
	private void populateAddLayersList(View view){
		this.selectedBaseLayerField = (TextView) view.findViewById(R.id.selectedBaseLayer);
		
		if(this.selectedBaseLayer != null){
			this.selectedBaseLayerField.setText(this.selectedBaseLayer.getName());
		}
		
		this.listView = (ListView) view.findViewById(R.id.layersListView);
		this.layersAdapter = new ChooseBaseLayerAdapter(getActivity(),
				R.layout.base_layer_list_item, this, creatingProject, this.selectedBaseLayer);
		this.listView.setAdapter(this.layersAdapter);
		
		if(!creatingProject){
			this.layerLoaderCallbacks = new ChooseBaseLayerLoaderCallbacks(getActivity(), this.layersAdapter, R.id.loader_layers_choose_baselayer);
		}else{
			Project newProject = ArbiterProject.getArbiterProject().getNewProject();
			ArrayList<Layer> newLayers = newProject.getLayers();
			
			int count = newLayers.size();
			
			ArrayList<Layer> deepCopy = new ArrayList<Layer>(count + 1);
			
			for(int i = 0; i < count; i++){
				deepCopy.add(newLayers.get(i));
			}
			
			deepCopy.add(new Layer(new BaseLayer("OpenStreetMap", null, null, null)));
			
			this.layersAdapter.setData(deepCopy);
		}
	}

	@Override
	public void updateBaselayer(BaseLayer baseLayer) {
	
		this.selectedBaseLayerField.setText(baseLayer.getName());
		
		this.baseLayer = baseLayer;
	}
}
