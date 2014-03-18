package com.lmn.Arbiter_Android.Dialog.Dialogs;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.ImageButton;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.ConnectivityListeners.AddLayersConnectivityListener;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.GeometryEditor.GeometryEditor;
import com.lmn.Arbiter_Android.ListAdapters.LayerListAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.LayerLoaderCallbacks;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;
import com.lmn.Arbiter_Android.OrderLayers.OrderLayersViewController;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class LayersDialog extends ArbiterDialogFragment{
	
	private ListView listView;
	private LayerListAdapter layersAdapter;
	@SuppressWarnings("unused")
	private LayerLoaderCallbacks layerLoaderCallbacks;
	
	@SuppressWarnings("unused")
	private AddLayersConnectivityListener connectivityListener;
	
	private MapChangeListener mapChangeListener;
	private ImageButton addLayersBtn;
	private ImageButton orderLayersBtn;
	private ImageButton cancelOrderLayersBtn;
	private ImageButton doneOrderingLayersBtn;
	private OrderLayersViewController orderLayersController;
	
	public static LayersDialog newInstance(String title, String ok, 
			String cancel, int layout){
		LayersDialog frag = new LayersDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setLayout(layout);
		
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		
		try {
			this.mapChangeListener = (MapChangeListener) this.getActivity();
		} catch (ClassCastException e){
			throw new ClassCastException(this.getActivity().toString() 
					+ " must implement MapChangeListener");
		}
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		this.getActivity().getSupportLoaderManager().destroyLoader(R.id.loader_layers);
	}
	
	@Override
	public void onPositiveClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCreateDialog(View view) {
		if(view != null){
			populateListView(view);
			registerListeners(view);
		}
	}
	
	private void displayLayersLimit(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setIcon(R.drawable.icon);
		builder.setTitle(R.string.layers_limit);
		builder.setMessage(R.string.too_many_layers);
		
		builder.create().show();
	}
	
	public void populateListView(View view){
		this.listView = (ListView) view.findViewById(R.id.layersListView);
		this.layersAdapter = new LayerListAdapter(this.getActivity(), R.layout.layers_list_item);
		this.listView.setAdapter(this.layersAdapter);
		
		this.layerLoaderCallbacks = new LayerLoaderCallbacks(this.getActivity(), this.layersAdapter, R.id.loader_layers);
	}
	
	// Return true if not editing
    private boolean makeSureNotEditing(){
    		
		int editMode = mapChangeListener.getMapChangeHelper().getEditMode();
		
		if(editMode == GeometryEditor.Mode.OFF || editMode == GeometryEditor.Mode.SELECT){
			return true;
		}
			
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle(R.string.finish_editing_title);
		builder.setMessage(R.string.finish_editing_message);
		builder.setIcon(R.drawable.icon);
		builder.setPositiveButton(android.R.string.ok, null);
		
		builder.create().show();
		
		return false;
    }
    
	public void registerListeners(View view){
		this.addLayersBtn = (ImageButton) view.findViewById(R.id.add_layers_button);
		this.orderLayersBtn = (ImageButton) view.findViewById(R.id.layer_order);
		this.cancelOrderLayersBtn = (ImageButton) view.findViewById(R.id.cancelOrderingLayers);
		this.doneOrderingLayersBtn = (ImageButton) view.findViewById(R.id.doneOrderingLayers);
		
		this.orderLayersController = new OrderLayersViewController(this.addLayersBtn, this.orderLayersBtn,
				this.cancelOrderLayersBtn, this.doneOrderingLayersBtn, this.layersAdapter);
		
		final LayersDialog frag = this;
			
			connectivityListener = new AddLayersConnectivityListener(
					getActivity().getApplicationContext(), this.addLayersBtn);
			
		this.addLayersBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				if(makeSureNotEditing()){
					if(layersAdapter.getCount() < 5){
						// Open the add layers dialog
						(new ArbiterDialogs(getActivity().getApplicationContext(), getActivity().getResources(), 
								getActivity().getSupportFragmentManager())).showAddLayersDialog(frag.getCopyOfLayers());
					}else{
						 displayLayersLimit();
					}
				}
			}
		});
		
		this.orderLayersBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				
				if(makeSureNotEditing()){
					frag.getActivity().runOnUiThread(new Runnable(){
						@Override
						public void run(){
							orderLayersController.beginOrderLayersMode();
						}
					});
				}
			}
		});
		
		this.cancelOrderLayersBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				layersAdapter.setData(layersAdapter.getOrderLayersModel().getBackup());
				
				orderLayersController.endOrderLayersMode();
			}
		});
		
		this.doneOrderingLayersBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				
				saveLayersOrder(new Runnable(){
					@Override
					public void run(){
						orderLayersController.endOrderLayersMode();
						
						mapChangeListener.getMapChangeHelper().onLayerOrderChanged();
					}
				});
			}
		});
	}
	private SQLiteDatabase getProjectDb(){
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(getActivity());
		
		return ProjectDatabaseHelper.getHelper(getActivity().getApplicationContext(),
				ProjectStructure.getProjectPath(projectName), false).getWritableDatabase();
	}
	
	private void saveLayersOrder(final Runnable onSaveComplete){
		
		String title = getActivity().getResources().getString(R.string.loading);
		
		String message = getActivity().getResources().getString(R.string.please_wait);
		
		final ProgressDialog saveProgressDialog = ProgressDialog.show(getActivity(), title, message, true);
		
		CommandExecutor.runProcess(new Runnable(){
			
			@Override
			public void run(){
				
				LayersHelper.getLayersHelper().updateLayers(getProjectDb(),
						getActivity().getApplicationContext(),
						layersAdapter.getLayers());
				
				getActivity().runOnUiThread(new Runnable(){
					@Override
					public void run(){
						onSaveComplete.run();
						
						saveProgressDialog.dismiss();
					}
				});
			}
		});
	}
	
	public ArrayList<Layer> getCopyOfLayers(){
		ArrayList<Layer> layers = this.layersAdapter.getLayers();
		
		final ArrayList<Layer> mLayers = new ArrayList<Layer>(layers.size());
		
		// Make a deep copy of the layers
		for(int i = 0; i < layers.size(); i++){
			mLayers.add(new Layer(layers.get(i)));
		}
				
		Log.w("LAYERSDIALOG", "LAYERSDIALOG count: " + mLayers.size());
		return mLayers;
	}
}
