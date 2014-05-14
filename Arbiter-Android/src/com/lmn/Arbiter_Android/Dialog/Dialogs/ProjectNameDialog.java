package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.ConnectivityListeners.ConnectivityListener;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;

public class ProjectNameDialog extends ArbiterDialogFragment{
	private View view;
	private ArbiterDialogs arbiterDialogs;
	private ConnectivityListener connectivityListener;
	
	public static ProjectNameDialog newInstance(String title, String ok, 
			String cancel, int layout, ConnectivityListener connectivityListener){
		
		final ProjectNameDialog frag = new ProjectNameDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		frag.connectivityListener = connectivityListener;
		
		frag.setValidatingClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				if(frag.connectivityListener != null && frag.connectivityListener.isConnected()){
					frag.onPositiveClick();
				}else{
					Util.showNoNetworkDialog(frag.getActivity());
				}
			}
		});
		
		return frag;
	}

	private ArbiterDialogs getArbiterDialogs(){
		if(arbiterDialogs == null){
			arbiterDialogs = new ArbiterDialogs(getActivity().getApplicationContext(), getActivity().getApplicationContext().getResources(),
								getActivity().getSupportFragmentManager());
		}
		
		return arbiterDialogs;
	}
	
	@Override
	public void onPositiveClick() {
		EditText projectNameField = (EditText) view.findViewById(R.id.project_name_edittext);
		ArbiterProject arbiterProject = ArbiterProject.getArbiterProject();
		
		arbiterProject.createNewProject(projectNameField.getText().toString());
		
		getArbiterDialogs().showAddLayersDialog(true, connectivityListener);
		
		dismiss();
	}

	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCreateDialog(View view) {
		this.view = view;
	}
}
