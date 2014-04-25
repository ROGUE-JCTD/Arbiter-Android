package com.lmn.Arbiter_Android.OnReturnToMap;

import java.util.ArrayList;

public class OnReturnToMap {

	private ArrayList<ReturnToMapJob> jobs;
	private int jobsLength;
	
	private OnReturnToMap(){
		
		this.jobs = new ArrayList<ReturnToMapJob>();
		this.jobsLength = 0;
	}
	
	private static OnReturnToMap onReturnToMap;
	
	public static OnReturnToMap getOnReturnToMap(){
		
		if(onReturnToMap == null){
			onReturnToMap = new OnReturnToMap();
		}
		
		return onReturnToMap;
	}
	
	public void push(ReturnToMapJob job){
		this.jobs.add(job);
		this.jobsLength++;
	}
	
	public ReturnToMapJob pop(){
		
		ReturnToMapJob job = null;
		
		if(this.jobsLength > 0){
			
			job = this.jobs.remove(0);
			this.jobsLength--;
		}
		
		return job;
	}
}
