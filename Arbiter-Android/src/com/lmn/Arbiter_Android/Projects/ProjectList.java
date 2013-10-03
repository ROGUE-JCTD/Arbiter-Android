package com.lmn.Arbiter_Android.Projects;

import com.lmn.Arbiter_Android.ArbiterList;

public class ProjectList extends ArbiterList<ProjectListItem>{

	public ProjectList(){
		super();
		populateLayerList();
	}

	/**
	 * Populate the layer list
	 */
	public void populateLayerList(){
		this.list.add(new ProjectListItem("Project 1 [current]"));
		this.list.add(new ProjectListItem("Project 2"));
	}
	
	@Override
	public void onAddItem(ProjectListItem item) {
		
	}

	@Override
	public void onRemoveItem(ProjectListItem item) {
		
	}
}
