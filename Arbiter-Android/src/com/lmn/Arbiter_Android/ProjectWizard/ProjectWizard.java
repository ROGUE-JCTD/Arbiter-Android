package com.lmn.Arbiter_Android.ProjectWizard;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.ProjectWizard.BuildingBlocks.Step;

/**
 * Class for a Project Wizard
 * @author Zusy
 * 
 */
public class ProjectWizard {
	private ArrayList<Step> steps;
	private int prevStep;
	
	/**
	 * Initialize the Project Wizard
	 */
	public ProjectWizard(){
		this.steps = new ArrayList<Step>();
		this.prevStep = -1;
	}
	
	/**
	 * Create the dialog for the wizard, called by startWizard
	 * @param step The first step for the dialog
	 */
	public void createWizard(Step step){
		if(step != null){
			ProjectWizardDialogFragment.newInstance(step);
		}
	}
	
	/**
	 * Start the wizard
	 */
	public void startWizard(){
			createWizard(nextStep());
	}
	
	/**
	 * Add a new step to the wizard
	 * @param newStep The new step to be added to the wizard
	 */
	public void addStep(Step newStep){
		steps.add(newStep);
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
}
