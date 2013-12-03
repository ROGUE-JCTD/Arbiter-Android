package com.lmn.Arbiter_Android.Dialog.Dialogs;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

public class FeatureDialog extends ArbiterDialogFragment{
	private ArbiterProject arbiterProject;
	private Feature feature;
	
	public static FeatureDialog newInstance(String title, String ok, 
			String cancel, int layout, Feature feature){
		FeatureDialog frag = new FeatureDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		frag.arbiterProject = ArbiterProject.getArbiterProject();
		frag.feature = feature;
		
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
	public void onPositiveClick() {
		final Context context = getActivity().getApplicationContext();
		
	}
	
	@Override
	public void onNegativeClick() {
		
	}

	@Override
	public void beforeCreateDialog(View view) {
		if(view != null){
			populateView(view);
			registerListeners(view);
		}
	}
	
	private void populateView(View view){
		FeatureDialogBuilder builder = new FeatureDialogBuilder(getActivity(), view, feature);
		builder.build();
	}
	
	
	private void registerListeners(View view){
		
	}
}
