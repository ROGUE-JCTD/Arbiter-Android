package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Loaders.NotificationsLoader;
import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class SwitchProjectDialog extends ArbiterDialogFragment{
	private String newProjectName;
	
	public static SwitchProjectDialog newInstance(String title, String ok, 
			String cancel, int layout, String newProjectName){
		SwitchProjectDialog frag = new SwitchProjectDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		frag.newProjectName = newProjectName;
		
		return frag;
	}
	
	@Override
	public void onPositiveClick() {
		ArbiterProject.getArbiterProject().setOpenProject(
				getActivity().getApplicationContext(),
				newProjectName);
		
		LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
			.sendBroadcast(new Intent(ProjectsListLoader.PROJECT_LIST_UPDATED));
		
		LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
			.sendBroadcast(new Intent(NotificationsLoader.NOTIFICATIONS_UPDATED));
		
		SQLiteDatabase db = ApplicationDatabaseHelper.getHelper(getActivity().getApplicationContext()).getWritableDatabase();
		
		PreferencesHelper.getHelper().put(db, getActivity().getApplicationContext(), PreferencesHelper.SWITCHED_PROJECT, "true");
		
		this.getActivity().finish();
	}

	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCreateDialog(View view) {
		
	}
}
