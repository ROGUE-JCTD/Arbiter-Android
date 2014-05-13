package com.lmn.Arbiter_Android.OnAddingGeometryPart;

import java.util.ArrayList;

public class OnAddingGeometryPart {

	private ArrayList<OnAddingGeometryPartJob> jobs;
	private int jobCount;
	
	private static OnAddingGeometryPart instance;
	
	public static OnAddingGeometryPart getInstance(){
		
		if(instance == null){
			instance = new OnAddingGeometryPart();
		}
		
		return instance;
	}
	
	private OnAddingGeometryPart(){
		jobs = new ArrayList<OnAddingGeometryPartJob>();
		jobCount = 0;
	}
	
	public void add(OnAddingGeometryPartJob job){
		
		jobs.add(job);
		
		jobCount++;
	}
	
	private OnAddingGeometryPartJob pop(){
		
		if(jobCount > 0){
			
			jobCount--;
			
			return jobs.remove(0);
		}
		
		return null;
	}
	
	public void checked(boolean isAddingPartAlready){
		
		runJobs(isAddingPartAlready);
	}
	
	private void runJobs(boolean isAddingPartAlready){
		
		OnAddingGeometryPartJob job = null;
		
		while(jobCount > 0){
			
			job = pop();
			
			job.run(isAddingPartAlready);
		}
	}
}
