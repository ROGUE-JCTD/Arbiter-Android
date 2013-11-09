package com.lmn.Arbiter_Android.ListAdapters;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.ProjectsActivity;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ProjectsHelper;
import com.lmn.Arbiter_Android.Dialog.Dialogs.SwitchProjectDialog;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class ProjectListAdapter extends BaseAdapter{
	private Project[] items;
	private final LayoutInflater inflater;
	private final Context context;
	private final ProjectsActivity activity;
	
	public ProjectListAdapter(ProjectsActivity activity){
			this.inflater = LayoutInflater.from(activity.getApplicationContext());
			this.items = new Project[0];
			this.context = activity.getApplicationContext();
			this.activity = activity;
	}
	
	public void setData(Project[] data){
		items = data;
		
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
				if(project.getId() != ArbiterProject.
						getArbiterProject().getOpenProject(activity)){
					Resources resources = activity.getResources();
					String title = resources.getString(R.string.switch_project_title);
					String ok = resources.getString(android.R.string.ok);
					String cancel = resources.getString(android.R.string.cancel);
					int layout = R.layout.switch_project;
					
					DialogFragment dialog = SwitchProjectDialog.newInstance(
							title, ok, cancel, layout, project.getId(), 
							project.getProjectName(), project.includeDefaultLayer());
					dialog.show(activity.getSupportFragmentManager(), "switchProjectDialog");
				}
			}
		});
		
		if(project != null){
			TextView projectNameTextView = (TextView) view.findViewById(R.id.projectName);
			
			if(projectNameTextView != null){
				String name = project.getProjectName();
				if(project.getId() == ArbiterProject.getArbiterProject().
						getOpenProject(activity)){
					name += " [current]";
				}
				
				projectNameTextView.setText(name);
			}
			
			ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteProject);
			
			if(deleteButton != null){
				deleteButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						CommandExecutor.runProcess(new Runnable(){
							@Override
							public void run() {
								
								ApplicationDatabaseHelper helper = ApplicationDatabaseHelper.getHelper(context);
								ProjectStructure.getProjectStructure().deleteProject(activity, project.getProjectName());
								ProjectsHelper.getProjectsHelper().delete(helper.getWritableDatabase(), activity, project, new Runnable(){
									@Override
									public void run(){
										ProjectStructure.getProjectStructure().ensureProjectExists(activity);
									}
								});
							}
							
						});
					}
					
				});
			}
		}
		
		return view;
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
		return getItem(position).getId();
	}

}
