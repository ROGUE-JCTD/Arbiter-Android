package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.ToggleButton;
import android.widget.ImageButton;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.ListAdapters.LayerListAdapter;
import com.lmn.Arbiter_Android.ListItems.Layer;
import com.lmn.Arbiter_Android.LoaderCallbacks.LayerLoaderCallbacks;

public class LayersDialog extends ArbiterDialogFragment{
	private ListView listView;
	private LayerListAdapter layersAdapter;
	@SuppressWarnings("unused")
	private LayerLoaderCallbacks layerLoaderCallbacks;
	
	public static LayersDialog newInstance(String title, String ok, 
			String cancel, int layout){
		LayersDialog frag = new LayersDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
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
	
	public void toggleLayerVisibility(View view){
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();
		
		if (on) {
			
		} else {
			
		}
	}

	@Override
	public void beforeCreateDialog(View view) {
		if(view != null){
			populateListView(view);
			registerListeners(view);
		}
	}
	
	public void registerListeners(View view){
		ImageButton button = (ImageButton) view.findViewById(R.id.add_layers_button);
		final LayersDialog frag = this;
		
		if(button != null){
			
			button.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// Open the add layers dialog
					(new ArbiterDialogs(getActivity().getResources(), 
							getActivity().getSupportFragmentManager())).showAddLayersDialog(false, frag.getCopyOfLayers());
				}
			});
		}
	}
	
	public void populateListView(View view){
		this.listView = (ListView) view.findViewById(R.id.layersListView);
		this.layersAdapter = new LayerListAdapter(this.getActivity().
				getApplicationContext(), R.layout.layers_list_item);
		this.listView.setAdapter(this.layersAdapter);
		
		this.layerLoaderCallbacks = new LayerLoaderCallbacks(this, this.layersAdapter, R.id.loader_layers);
	}
	
	public Layer[] getCopyOfLayers(){
		Layer[] layers = this.layersAdapter.getLayers();
		final Layer[] mLayers = new Layer[layers.length];
		
		// Make a deep copy of the layers
		for(int i = 0; i < layers.length; i++){
			mLayers[i] = new Layer(layers[i]);
		}
				
		Log.w("LAYERSDIALOG", "LAYERSDIALOG count: " + mLayers.length);
		return mLayers;
	}
}
