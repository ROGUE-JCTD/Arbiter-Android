package com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor;

import java.util.LinkedList;

import android.util.Log;

public class CommandExecutor implements Runnable{
	private LinkedList<Runnable> commandList;
	private final Object mutex;
	
	public CommandExecutor(LinkedList<Runnable> list, Object mutex){
		this.commandList = list;
		this.mutex = mutex;
	}
	
	@Override
	public void run() {
		while(true){ // keep the thread running
			Log.w("COMMAND_EXECUTOR", "COMMAND EXECUTOR IS RUNNING");
			synchronized(mutex){
				// If the commandList is empty, wait until it's not
				while(commandList.isEmpty()){ 
					try {
						mutex.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
				}
				
				// Remove the head of the list
				commandList.poll().run();
			}
		}	
	}

}
