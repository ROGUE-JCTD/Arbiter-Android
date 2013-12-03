package com.lmn.Arbiter_Android;

import java.util.HashMap;

import android.content.ContentValues;

public class Util {
	
	public Util(){}
	
	public boolean convertIntToBoolean(int number){
		return (number > 0) ? true : false;
	}
	
	public ContentValues contentValuesFromHashMap(HashMap<String, String> hashmap){
		ContentValues values = new ContentValues();
		
		for(String key : hashmap.keySet()){
			values.put(key, hashmap.get(key));
		}
		
		return values;
	}
}
