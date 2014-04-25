package com.lmn.Arbiter_Android.AppFinishedLoading;

import java.util.ArrayList;

import android.util.Log;

public class AppFinishedLoading {

	private boolean finishedLoading;
	private ArrayList<AppFinishedLoadingJob> jobs;
	
	private AppFinishedLoading(){
		finishedLoading = false;
		this.jobs = new ArrayList<AppFinishedLoadingJob>();
	}
	
	public static AppFinishedLoading appFinishedLoading;
	
	public static AppFinishedLoading getInstance(){
		
		if(appFinishedLoading == null){
			appFinishedLoading = new AppFinishedLoading();
		}
		
		return appFinishedLoading;
	}
	
	public void setFinishedLoading(boolean finishedLoading){
		Log.w("AppFinishedLoading", "AppFinishedLoading - setFinishedLoading = " + finishedLoading);
		
		this.finishedLoading = finishedLoading;
		
		if(this.finishedLoading){
			this.runJobs();
		}
	}
	
	private void runJobs(){
		
		Log.w("AppFinishedLoading", "AppFinishedLoading - running jobs");
		
		AppFinishedLoadingJob job = jobs.remove(0);
		
		while(job != null){
			
			job.run();
			
			job = jobs.remove(0);
		}
	}
	
	public void onAppFinishedLoading(AppFinishedLoadingJob job){
		
		if(this.finishedLoading){
			job.run();
		}else{
			this.jobs.add(job);
		}
	}
}
