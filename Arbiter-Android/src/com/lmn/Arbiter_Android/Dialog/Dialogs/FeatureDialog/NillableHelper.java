package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.HashMap;

public class NillableHelper {

	private HashMap<String, Boolean> attributes;
	
	public NillableHelper(){
		
		attributes = new HashMap<String, Boolean>();
	}
	
	public NillableHelper(int capacity){
		
		attributes = new HashMap<String, Boolean>(capacity);
	}
	
	public void addAttribute(String name, boolean isNillable){
		
		attributes.put(name, isNillable);
	}
	
	public boolean isNillable(String name){
		
		return attributes.get(name);
	}
}
