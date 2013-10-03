package com.lmn.Arbiter_Android.MenuEvents;

import android.content.res.Resources;
import android.support.v4.app.FragmentManager;

import com.lmn.Arbiter_Android.ProjectWizard.ProjectWizard;

public class ProjectMenuEvents extends MenuEvents {
	private FragmentManager fragManager;
	private Resources resources;
	
	public ProjectMenuEvents(FragmentManager fragManager, Resources resources){
		super();
		this.fragManager = fragManager;
		this.resources = resources;
	}
	
	public void startProjectWizard(){
		(new ProjectWizard(fragManager, resources)).startWizard();
	}
}
