package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.content.Context;
import android.view.View;

import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;

public class GoOfflineDialog extends ArbiterDialogFragment{
	Project project;
	
	public static GoOfflineDialog newInstance(String title, String ok, 
			String cancel, int layout, Project project){
		GoOfflineDialog frag = new GoOfflineDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		frag.project = project;
		
		return frag;
	}

	@Override
	public void onPositiveClick() {
		// Write the project and layers to the database
		if(project != null){
			final Project newProject = new Project(project);
			final Context context = this.getActivity().getApplicationContext();
			
			CommandExecutor.runProcess(new Runnable(){

				@Override
				public void run() {
					GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(context);
					ProjectsHelper.getProjectsHelper().insert(helper.getWritableDatabase(), context, newProject);
				}
				
			});
		}
	}

	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCreateDialog(View view) {
		// TODO Auto-generated method stub
		
	}
}
