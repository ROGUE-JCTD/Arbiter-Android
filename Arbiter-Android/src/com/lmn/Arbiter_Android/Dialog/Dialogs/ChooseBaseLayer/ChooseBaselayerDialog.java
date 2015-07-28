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
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.Activities.HasThreadPool;
import com.lmn.Arbiter_Android.BaseClasses.BaseLayer;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.ConnectivityListeners.ConnectivityListener;
import com.lmn.Arbiter_Android.CookieManager.ArbiterCookieManager;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesetsHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ProgressDialog.SyncProgressDialog;
import com.lmn.Arbiter_Android.ListAdapters.ChooseBaseLayerAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.ChooseBaseLayerLoaderCallbacks;
import com.lmn.Arbiter_Android.Loaders.BaseLayerLoader;
import com.lmn.Arbiter_Android.Loaders.LayersListLoader;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;
import com.lmn.Arbiter_Android.Settings.Settings;

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
	private MapChangeListener mapChangeListener;
	private BaseLayer startingBaseLayer;
	private ConnectivityListener connectivityListener;
	private HasThreadPool hasThreadPool;
	
	public static ChooseBaselayerDialog newInstance(String title, String ok, 
			String cancel, int layout, boolean creatingProject, BaseLayer baseLayer,
			ConnectivityListener connectivityListener, HasThreadPool hasThreadPool){
		
		final ChooseBaselayerDialog frag = new ChooseBaselayerDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		frag.creatingProject = creatingProject;
		frag.selectedBaseLayerField = null;
		frag.startingBaseLayer = baseLayer;
		frag.baseLayer = baseLayer;
		frag.arbiterProject = ArbiterProject.getArbiterProject();
		frag.mapChangeListener = null;
		frag.connectivityListener = connectivityListener;
		frag.hasThreadPool = hasThreadPool;
		
		Log.w("ChooseBaseLayerDialog", "ChooseBaseLayerDialog connectivityListener " + ((connectivityListener == null) ? "is null" : "isn't null"));
		
		frag.setValidatingClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				if(frag.connectivityListener != null && frag.connectivityListener.isConnected()){
					frag.onPositiveClick();
				}else{
					frag.onPositiveClick();

					//Util.showNoNetworkDialog(frag.getActivity());
				}
			}
		});
		
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
			
			String selectedName = baseLayer.getName();
			String Name = startingBaseLayer.getName();
			
			if(selectedName == null){
				selectedName = "null";
			}
			
			if(Name == null){
				Name = "null";
			}
			
			if(selectedName != Name && !selectedName.equals(Name)){
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
								
								SyncProgressDialog.show(activity);
								
								hasThreadPool.getThreadPool().execute(new Runnable(){
									@Override
									public void run(){
										
										new ArbiterCookieManager(context).updateAllCookies();
										
										activity.runOnUiThread(new Runnable(){
											@Override
											public void run(){
												
												mapChangeListener.getMapChangeHelper().cacheBaseLayer();
												
												dismiss();
											}
										});
									}
								});
							}
						});
					}
				});
			}
		}else{
			Project newProject = ArbiterProject.getArbiterProject().getNewProject();
			
			// Set the base layer for the new project
			newProject.setBaseLayer(baseLayer);
			
			new Settings(getActivity()).displaySettingsDialog(true);
    		
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
			populateAddLayersList(view);
		}
	}
	
	private void populateAddLayersList(View view){
		this.selectedBaseLayerField = (TextView) view.findViewById(R.id.selectedBaseLayer);
		
		if(this.baseLayer != null){
			this.selectedBaseLayerField.setText(this.baseLayer.getName());
		}
		
		this.listView = (ListView) view.findViewById(R.id.layersListView);
		this.layersAdapter = new ChooseBaseLayerAdapter(getActivity(),
				R.layout.base_layer_list_item, this, creatingProject, this.baseLayer);
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
			
			Log.w("ChooseBaseLayer", "ChooseBaseLayer adding default base layer");
			BaseLayer baseLayer = BaseLayer.createOSMBaseLayer();
			
			deepCopy.add(new Layer(baseLayer));

			ArrayList<Tileset> tilesets = TilesetsHelper.getTilesetsHelper().getAll(
					ApplicationDatabaseHelper.getHelper(
							getActivity().getApplicationContext()).getWritableDatabase());
			for (int i = 0; i < tilesets.size(); i++){
				if (tilesets.get(i).getFilesize() > 0) {
					deepCopy.add(new Layer(tilesets.get(i).toBaseLayer()));
				}
			}
			
			this.layersAdapter.setData(deepCopy);
		}
	}

	@Override
	public void updateBaselayer(BaseLayer baseLayer) {
	
		this.selectedBaseLayerField.setText(baseLayer.getName());
		
		this.baseLayer = baseLayer;
	}
}
