package com.lmn.Arbiter_Android.ProjectWizard;

import java.util.ArrayList;

import android.content.res.Resources;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ProjectWizard.Steps.CreateProject;
import com.lmn.Arbiter_Android.ProjectWizard.Steps.Step;

public class PopulateProjectWizard {
	private ArrayList<Step> steps;
	private Resources resources;
	private ProjectWizard projectWizard;
	
	public PopulateProjectWizard(Resources resources, ArrayList<Step> steps,
			ProjectWizard projectWizard){
		this.steps = steps;
		this.resources = resources;
		this.projectWizard = projectWizard;
	}
	
	public void populate(){
		addCreateProject();
		addChooseName();
		addDefaultServerURL();
		addDefaultServerCredentials();
		addDefaultServerName();
		//addAddLayers();
		//addChooseAOI();
	}
	
	public void addCreateProject(){
		String title = resources.getString(R.string.create_project_title);
		int layout = R.layout.create_project;
		String ok = resources.getString(R.string.project_wizard_ok);
		String cancel = resources.getString(R.string.project_wizard_cancel);
		
		steps.add(new CreateProject(title, layout, ok, cancel, projectWizard));
	}
	
	public void addChooseName(){
		String title = resources.getString(R.string.choose_name_title);
		int layout = R.layout.choose_name;
		String ok = resources.getString(R.string.project_wizard_ok);
		String cancel = resources.getString(R.string.project_wizard_cancel);
		
		steps.add(new CreateProject(title, layout, ok, cancel, projectWizard));
	}
	
	public void addDefaultServerURL(){
		String title = resources.getString(R.string.default_server_url_title);
		int layout = R.layout.default_server_url;
		String ok = resources.getString(R.string.project_wizard_ok);
		String cancel = resources.getString(R.string.project_wizard_cancel);
		
		steps.add(new CreateProject(title, layout, ok, cancel, projectWizard));
	}
	
	public void addDefaultServerCredentials(){
		String title = resources.getString(R.string.default_server_login_title);
		int layout = R.layout.default_server_credentials;
		String ok = resources.getString(R.string.project_wizard_ok);
		String cancel = resources.getString(R.string.project_wizard_cancel);
		
		steps.add(new CreateProject(title, layout, ok, cancel, projectWizard));
	}
	
	public void addDefaultServerName(){
		String title = resources.getString(R.string.default_server_name_title);
		int layout = R.layout.default_server_name;
		String ok = resources.getString(R.string.project_wizard_ok);
		String cancel = resources.getString(R.string.project_wizard_cancel);
		
		steps.add(new CreateProject(title, layout, ok, cancel, projectWizard));
	}
	
	/*
	public void addAddLayers(){
		String title = resources.getString(R.string.create_project_title);
		int layout = R.layout.create_project;
		String ok = resources.getString(R.string.project_wizard_ok);
		String cancel = resources.getString(R.string.project_wizard_cancel);
		
		steps.add(new CreateProject(title, layout, ok, cancel, projectWizard));
	}
	
	public void addChooseAOI(){
		String title = resources.getString(R.string.create_project_title);
		int layout = R.layout.create_project;
		String ok = resources.getString(R.string.project_wizard_ok);
		String cancel = resources.getString(R.string.project_wizard_cancel);
		
		steps.add(new CreateProject(title, layout, ok, cancel, projectWizard));
	}*/
}
