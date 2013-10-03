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
	
	
	/**
	 * Initialize the TransitionManager
	 * @param fragManager The FragmentManager to display the dialog
	 */
	public TransitionManager(FragmentManager fragManager){
		this.steps = new ArrayList<Step>();
		this.prevStep = -1;
		this.fragManager = fragManager;
	}

	/**
	 * Show the dialog initialized with the given step
	 * @param step The step to display in the dialog
	 */
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
	
	/**
	 * Display the next step in the list 
	 */
	public void transitionNext() {
		if(hasNext()){
			showStep(nextStep());
		}
	}

	/**
	 * Get the step at the specified index
	 * @param index The index of the requested step
	 * @return The requested step or null if the index was out of bounds
	 */
	public Step getStep(int index){
		int size = steps.size();
		
		Step step = null;
		
		if((index >= 0) && (index < size - 1)){
			step = steps.get(index);
		}
		
		return step;
	}
	
	/**
	 * Skip to the step at the specified index
	 * @param Index of step to skip to
	 */
	public void transitionSkip(int index) {
		Step nextStep = getStep(index);
		
		if(nextStep != null){
			//Update prevStep for the skip
			prevStep = index; 
			showStep(nextStep);
		}
	}
	
	/**
	 * Get the current step
	 */
	public int getCurrentStep(){
		return prevStep;
	}
	
	/**
	 * Get the list of steps
	 * @return The list of steps
	 */
	public ArrayList<Step> getSteps() {
		return steps;
	}
	
	/**
	 * Handle the transition based on the current step
	 */
	public void handleSkip(){
		switch(prevStep){
			// Skip the server url
			case 2:
				//transitionManager.transitionSkip();
				break;
			// Skip the server login
			case 3:
				transitionSkip(4);
				break;
			// Skip the server name
			case 4:
				transitionSkip(5);
				break;
			// Skip add layers
			case 5:
				transitionSkip(6);
		}
	}
}
