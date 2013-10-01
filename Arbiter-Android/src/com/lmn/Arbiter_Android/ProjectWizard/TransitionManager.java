package com.lmn.Arbiter_Android.ProjectWizard;

import java.util.ArrayList;

//import android.app.DialogFragment;
//import android.app.FragmentManager;


import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.lmn.Arbiter_Android.ProjectWizard.Steps.Step;

public class TransitionManager {
	private ArrayList<Step> steps;
	private int prevStep;
	private FragmentManager fragManager;
	
	public TransitionManager(FragmentManager fragManager){
		this.steps = new ArrayList<Step>();
		this.prevStep = -1;
		this.fragManager = fragManager;
	}

	public void showStep(Step step){
		DialogFragment newFragment = ProjectWizardDialogFragment.newInstance(step);
		newFragment.show(fragManager, "dialog");
	}
	
	/**
	 * Is there another step in the wizard?
	 * @return	Whether or not there is another step
	 */
	public boolean hasNext(){
		boolean hasNext = false;
		int numSteps = steps.size();
		
		if(prevStep < (numSteps - 1)){
			hasNext = true;
		}
		
		return hasNext;
	}
	
	/**
	 * Get the next step
	 * @return	The next step to be executed
	 */
	public Step nextStep(){
		if(hasNext()){
			return steps.get(++prevStep);
		}
	
		return null;
	}
	
	public void transitionNext() {
		if(hasNext()){
			showStep(nextStep());
		}
	}

	public void transitionSkip() {
		
	}
	
	public int getCurrentStep(){
		return prevStep;
	}
	
	public ArrayList<Step> getSteps() {
		return steps;
	}
}
