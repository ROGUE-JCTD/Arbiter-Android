package com.lmn.Arbiter_Android.ListAdapters;

import java.util.LinkedList;
import java.util.List;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.ProjectsActivity;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.Dialog.Dialogs.SwitchProjectDialog;
import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class ProjectListAdapter extends BaseAdapter implements ArbiterAdapter<Project[]>{
	private Project[] items;
	private final LayoutInflater inflater;
	private final ProjectsActivity activity;
	
	public ProjectListAdapter(ProjectsActivity activity){
			this.inflater = LayoutInflater.from(activity.getApplicationContext());
			this.items = new Project[0];
			this.activity = activity;
	}
	
	public void setData(Project[] data){
		if(data == null) {
			items = null;
		} else {
			List<Project> projectList = new LinkedList<Project>();
			for (int i = 0; i < data.length; i++) {
				if(!data[i].getProjectName().equals(activity.getResources().getString(R.string.default_project_name))) {
					projectList.add(data[i]);
				}
			}
			items = projectList.toArray(new Project[projectList.size()]);
		}
		
		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		// Inflate the layout
		if(view == null){
			view = inflater.inflate(R.layout.project_list_item, null);
		} 
		
		final Project project = getItem(position);
		
		view.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if(!project.getProjectName().equals(ArbiterProject.
						getArbiterProject().getOpenProject(activity))){
					Resources resources = activity.getResources();
					String title = resources.getString(R.string.switch_project_title);
					String ok = resources.getString(android.R.string.ok);
					String cancel = resources.getString(android.R.string.cancel);
					int layout = R.layout.switch_project;
					
					DialogFragment dialog = SwitchProjectDialog.newInstance(
							title, ok, cancel, layout, 
							project.getProjectName());
					
					dialog.show(activity.getSupportFragmentManager(), "switchProjectDialog");
				}
			}
		});
		
		if(project != null){
			TextView projectNameTextView = (TextView) view.findViewById(R.id.projectName);
			
			if(projectNameTextView != null){
				String name = project.getProjectName();
				if(project.getProjectName().equals(ArbiterProject.getArbiterProject().
						getOpenProject(activity))){
					name += " [current]";
				}
				
				projectNameTextView.setText(name);
			}
			
			ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteProject);
			
			if(deleteButton != null){
				deleteButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						
						confirmDeleteProject(project);
					}
					
				});
			}
		}
		
		return view;
	}

	private void confirmDeleteProject(final Project project){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(R.string.warning);
		
		builder.setMessage(R.string.confirm_delete_project);
		
		builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				delete(project);
			}
		});
		
		builder.setNegativeButton(android.R.string.cancel, null);
		
		builder.create().show();
	}
	
	private void delete(final Project project){
		
		String title = activity.getResources().getString(R.string.loading);
		String message = activity.getResources().getString(R.string.please_wait);
		
		final ProgressDialog progressDialog = ProgressDialog.show(activity, title, message, true);
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run() {
				// Delete the corresponding project directory
				ProjectStructure.getProjectStructure().deleteProject(activity, project.getProjectName());
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
					
						// Make sure the Project list updates.
						LocalBroadcastManager.getInstance(activity.getApplicationContext())
							.sendBroadcast(new Intent(ProjectsListLoader.PROJECT_LIST_UPDATED));
						
						progressDialog.dismiss();
					}
				});
			}
			
		});
	}
	
	@Override
	public int getCount() {
		if(items == null){
			return 0;
		}
		
		return items.length;
	}

	@Override
	public Project getItem(int position) {
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
