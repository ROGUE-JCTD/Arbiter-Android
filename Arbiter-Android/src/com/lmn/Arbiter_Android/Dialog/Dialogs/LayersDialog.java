package com.lmn.Arbiter_Android.Dialog.Dialogs;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ImageButton;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.HasThreadPool;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.ConnectivityListeners.AddLayersConnectivityListener;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.GeometryEditor.GeometryEditor;
import com.lmn.Arbiter_Android.ListAdapters.BaseLayerList;
import com.lmn.Arbiter_Android.ListAdapters.OverlayList;
import com.lmn.Arbiter_Android.LoaderCallbacks.BaseLayerLoaderCallbacks;
import com.lmn.Arbiter_Android.LoaderCallbacks.LayerLoaderCallbacks;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;
import com.lmn.Arbiter_Android.OrderLayers.OrderLayersViewController;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class LayersDialog extends ArbiterDialogFragment{
	
	private OverlayList overlayList;
	private BaseLayerList baseLayerList;
	
	@SuppressWarnings("unused")
	private LayerLoaderCallbacks layerLoaderCallbacks;
	
	@SuppressWarnings("unused")
	private BaseLayerLoaderCallbacks baseLayerLoaderCallbacks;
	
	private AddLayersConnectivityListener connectivityListener;
	
	private MapChangeListener mapChangeListener;
	private ImageButton addLayersBtn;
	private ImageButton orderLayersBtn;
	private ImageButton cancelOrderLayersBtn;
	private ImageButton doneOrderingLayersBtn;
	private OrderLayersViewController orderLayersController;
	private HasThreadPool hasThreadPool;
	
	public static LayersDialog newInstance(String title, String ok, 
			String cancel, int layout, HasThreadPool hasThreadPool){
		LayersDialog frag = new LayersDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setLayout(layout);
		
		frag.hasThreadPool = hasThreadPool;
		
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
		this.getActivity().getSupportLoaderManager().destroyLoader(R.id.loader_base_layer_list);
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
		
		populateOverlayList(view);
		populateBaseLayerList(view);
	}
	
	private void populateOverlayList(View view){
		LinearLayout overlayList =	(LinearLayout) view.findViewById(R.id.overlaysList);
		
		this.overlayList = new OverlayList(overlayList, this.getActivity(), R.layout.layers_list_item, hasThreadPool);
		
		this.layerLoaderCallbacks = new LayerLoaderCallbacks(this.getActivity(), this.overlayList, R.id.loader_layers);
	}
	
	private void populateBaseLayerList(View view){
		LinearLayout baselayerList = (LinearLayout) view.findViewById(R.id.baselayerList);
		
		this.baseLayerList = new BaseLayerList(baselayerList, this.getActivity(), R.layout.base_layer_list_item, connectivityListener, hasThreadPool);
		
		this.baseLayerLoaderCallbacks = new BaseLayerLoaderCallbacks(this, this.baseLayerList, R.id.loader_base_layer_list);
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
		
		connectivityListener = new AddLayersConnectivityListener(getActivity(), this.addLayersBtn);
		
		populateListView(view);
		
		this.orderLayersController = new OrderLayersViewController(this.addLayersBtn, this.orderLayersBtn,
				this.cancelOrderLayersBtn, this.doneOrderingLayersBtn, this.overlayList);
		
		final LayersDialog frag = this;
			
		this.addLayersBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				if(makeSureNotEditing()){
					if(overlayList.getCount() < 5){
						
						Log.w("LayersDialog", "LayersDialog connectivityListener " + ((connectivityListener == null) ? "is null" : "isn't null"));
						// Open the add layers dialog
						(new ArbiterDialogs(getActivity().getApplicationContext(), getActivity().getResources(), 
								getActivity().getSupportFragmentManager())).showAddLayersDialog(frag.getCopyOfLayers(), connectivityListener, hasThreadPool);
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
				overlayList.setData(overlayList.getOrderLayersModel().getBackup());
				
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
						overlayList.getData());
				
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
		ArrayList<Layer> layers = this.overlayList.getData();
		
		final ArrayList<Layer> mLayers = new ArrayList<Layer>(layers.size());
		
		// Make a deep copy of the layers
		for(int i = 0; i < layers.size(); i++){
			mLayers.add(new Layer(layers.get(i)));
		}
				
		Log.w("LAYERSDIALOG", "LAYERSDIALOG count: " + mLayers.size());
		return mLayers;
	}
}
