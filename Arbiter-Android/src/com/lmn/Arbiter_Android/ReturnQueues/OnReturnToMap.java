package com.lmn.Arbiter_Android.ReturnQueues;


public class OnReturnToMap extends ArbiterQueue{
	
	private OnReturnToMap(){
		
		super();
	}
	
	private static OnReturnToMap onReturnToMap;
	
	public static OnReturnToMap getInstance(){
		
		if(onReturnToMap == null){
			onReturnToMap = new OnReturnToMap();
		}
		
		return onReturnToMap;
	}
}
