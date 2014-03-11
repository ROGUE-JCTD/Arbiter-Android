package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.ListAdapters.ChooseGeometryTypeAdapter;

public class ChooseGeometryTypeDialog extends ArbiterDialogFragment{
	public static final String TAG = "ChooseGeometryTypeDialog";
	
	private ListView listView;
	private ChooseGeometryTypeAdapter layersAdapter;
	private String featureType;
	private long layerId;
	private int mode;
	
	public static ChooseGeometryTypeDialog newInstance(String title, String cancel,
			String featureType, long layerId, int mode){
		
		ChooseGeometryTypeDialog frag = new ChooseGeometryTypeDialog();
		
		frag.setLayout(R.layout.choose_geometry_type);
		frag.setTitle(title);
		frag.setCancel(cancel);
		frag.featureType = featureType;
		frag.layerId = layerId;
		frag.mode = mode;
		
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
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
		}
	}
	
	public void populateListView(View view){
		this.listView = (ListView) view.findViewById(R.id.geometryTypeChooser);
		this.layersAdapter = new ChooseGeometryTypeAdapter(
				this, R.layout.choose_geometry_type_list_item,
				featureType, layerId, mode);
		
		this.listView.setAdapter(this.layersAdapter);
	}
}
