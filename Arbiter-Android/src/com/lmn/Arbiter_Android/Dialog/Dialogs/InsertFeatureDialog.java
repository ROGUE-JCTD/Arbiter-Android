package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.ListAdapters.InsertFeaturesListAdapter;
import com.lmn.Arbiter_Android.LoaderCallbacks.InsertFeatureLayersLoaderCallbacks;

public class InsertFeatureDialog extends ArbiterDialogFragment{
	public static final String TAG = "InsertFeatureDialog";
	
	private ListView listView;
	private InsertFeaturesListAdapter layersAdapter;
	@SuppressWarnings("unused")
	private InsertFeatureLayersLoaderCallbacks layerLoaderCallbacks;
	
	public static InsertFeatureDialog newInstance(String title, String cancel){
		InsertFeatureDialog frag = new InsertFeatureDialog();
		
		frag.setLayout(R.layout.insert_feature_dialog);
		frag.setTitle(title);
		frag.setCancel(cancel);
		
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
		
		this.getActivity().getSupportLoaderManager()
			.destroyLoader(R.id.loader_insert_feature_dialog);
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
	
	public void registerListeners(View view){
		
	}
	
	public void populateListView(View view){
		this.listView = (ListView) view.findViewById(R.id.layersListView);
		this.layersAdapter = new InsertFeaturesListAdapter(
				this, R.layout.insert_feature_list_item);
		
		this.listView.setAdapter(this.layersAdapter);
		
		this.layerLoaderCallbacks = new InsertFeatureLayersLoaderCallbacks(this.getActivity(),
				this.layersAdapter, R.id.loader_insert_feature_dialog);
	}
}
