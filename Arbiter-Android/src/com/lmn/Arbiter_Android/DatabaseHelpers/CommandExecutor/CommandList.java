package com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor;

import java.util.LinkedList;

import android.util.Log;

public class CommandList {
	private LinkedList<Runnable> commandList;
	private final Object mutex;
	
	private CommandList(){
		commandList = new LinkedList<Runnable>();
		mutex = new Object();
		Thread executor = new Thread(new CommandExecutor(commandList, mutex));
		executor.start();
	}
	
	private static CommandList dbHelper = null;
	
	public static CommandList getCommandList(){
		if(dbHelper == null){
			dbHelper = new CommandList();
		}
		
		return dbHelper;
	}
	
	public void queueCommand(Runnable command){
		Log.w("CommandList", "QUEUE THE COMMAND");
		synchronized(mutex){
			commandList.add(command);
			mutex.notify();
		}
	}
}
