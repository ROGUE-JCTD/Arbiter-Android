package com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandExecutor{
	public static ExecutorService exe = Executors.newSingleThreadExecutor();
	
	public static void runProcess(Runnable r){
		exe.execute(r);
	}
}


