package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.view.View;
import android.widget.EditText;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.Projects.ProjectComponents;

public class ProjectNameDialog extends ArbiterDialogFragment{
	//private CommandList commandList;
	private View view;
	private ArbiterDialogs arbiterDialogs;
	
	public static ProjectNameDialog newInstance(String title, String ok, 
			String cancel, int layout){
		ProjectNameDialog frag = new ProjectNameDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		return frag;
	}

	private ArbiterDialogs getArbiterDialogs(){
		if(arbiterDialogs == null){
			arbiterDialogs = new ArbiterDialogs(getActivity().getResources(),
								getActivity().getSupportFragmentManager());
		}
		
		return arbiterDialogs;
	}
	
	public void transitionNext(){
		getArbiterDialogs().showAddLayersDialog(true);
	}
	
	public void transitionBack(){
		
	}
	
	@Override
	public void onPositiveClick() {
		EditText projectNameField = (EditText) view.findViewById(R.id.project_name_edittext);
		ProjectComponents.getProjectComponents().setName(projectNameField.getText().toString());
		
		transitionNext();
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
