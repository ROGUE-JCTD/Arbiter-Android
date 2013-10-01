package com.lmn.Arbiter_Android.ProjectWizard;

import android.app.FragmentManager;
import android.content.res.Resources;
import android.util.Log;

import com.lmn.Arbiter_Android.ProjectWizard.Steps.Step;

/**
 * Class for a Project Wizard
 * @author Zusy
 * 
 */
public class ProjectWizard {
	private FragmentManager fragManager;
	private PopulateProjectWizard populateWizard;
	private TransitionManager transitionManager;
	
	/**
	 * Initialize the Project Wizard
	 */
	public ProjectWizard(FragmentManager fragManager, Resources resources){
		this.fragManager = fragManager;
		this.transitionManager = new TransitionManager(this.fragManager);
		
		this.populateWizard = new PopulateProjectWizard(resources, 
				transitionManager.getSteps(), this);
		this.populateWizard.populate();
	}
	
	/**
	 * Create the dialog for the wizard, called by startWizard
	 * @param step The first step for the dialog
	 */
	public void startWizard(){
		Step firstStep = transitionManager.nextStep();
		Log.w("Arbiter_Android", "START WIZARD");
		if(firstStep != null){
			transitionManager.showStep(firstStep);
		}
	}
	
	/**
	 * Transition for ok, skip, and back?
	 * @param state Perform ok, skip, or back transition (0, 1, 2)
	 */
	public void transition(Transition state){
		switch (state) {
			case OK:
				Log.w("Arbiter_Android", "OK HAS BEEN HIT!!");
				transitionManager.transitionNext();
				break;
				
			case SKIP:
				Log.w("Arbiter_Android", "CANCEL HAS BEEN HIT!!");
				transitionManager.transitionSkip();
				break;
				
			case BACK:
				Log.w("Arbiter_Android", "BACK HAS BEEN HIT!!");
				break;
				
			default:
				
				break;
		}
	}
}
