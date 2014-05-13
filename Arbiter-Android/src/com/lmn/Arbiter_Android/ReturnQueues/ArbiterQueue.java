package com.lmn.Arbiter_Android.ReturnQueues;

import java.util.ArrayList;

import android.app.Activity;

public class ArbiterQueue {

	private ArrayList<ReturnToActivityJob> jobs;
	private int jobsLength;
	
	protected ArbiterQueue(){
		
		this.jobs = new ArrayList<ReturnToActivityJob>();
		this.jobsLength = 0;
	}
	
	public void push(ReturnToActivityJob job){
		this.jobs.add(job);
		this.jobsLength++;
	}
	
	public ReturnToActivityJob pop(){
		
		ReturnToActivityJob job = null;
		
		if(this.jobsLength > 0){
			
			job = this.jobs.remove(0);
			this.jobsLength--;
		}
		
		return job;
	}
	
	public void executeJobs(Activity activity){
        
        ReturnToActivityJob job = pop();
        
        while(job != null){
        	
        	job.run(activity);
        	
        	job = pop();
        }
    }
}
