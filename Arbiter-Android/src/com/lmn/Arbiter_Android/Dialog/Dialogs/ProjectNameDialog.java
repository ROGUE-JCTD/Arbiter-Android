package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.support.v4.content.LocalBroadcastManager;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.DatabaseHelpers.DbHelpers;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandList;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;

public class ProjectNameDialog extends ArbiterDialogFragment{
	private CommandList commandList;
	private View view;
	
	public static ProjectNameDialog newInstance(String title, String ok, 
			String cancel, int layout){
		ProjectNameDialog frag = new ProjectNameDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		frag.commandList = CommandList.getCommandList();
		
		return frag;
	}

	@Override
	public void onPositiveClick() {
		final Context context = this.getActivity();
		
		commandList.queueCommand(new Runnable(){
			@Override
			public void run() {
				Log.w("EXECUTED_COMMAND", "INSERT PROJECT");
				GlobalDatabaseHelper helper = DbHelpers.getDbHelpers(context).getGlobalDbHelper();
				EditText projectNameField = (EditText) view.findViewById(R.id.project_name_edittext);
				
				
				helper.createProject(projectNameField.getText().toString());
				
				LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ProjectsListLoader.PROJECT_LIST_UPDATED));
			}
		});
		
		(new ArbiterDialogs(getActivity().getResources(), getActivity().getSupportFragmentManager())).showAddLayersDialog();
	}

	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerCustomListeners(View view) {
		// TODO Auto-generated method stub
		this.view = view;
	}
}
