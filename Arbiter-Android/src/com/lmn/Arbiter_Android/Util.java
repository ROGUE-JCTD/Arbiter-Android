package com.lmn.Arbiter_Android;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

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
	
	public static void showDialog(final Activity activity, int title, int message, 
			String optionalMessage, Integer negativeBtnText,
			Integer positiveBtnText, final Runnable onPositive){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		String msg = activity.getResources().getString(message);
		
		if(optionalMessage != null){
			msg += ": \n\t" + optionalMessage;
		}
		
		builder.setTitle(title);
		builder.setIcon(activity.getResources().getDrawable(R.drawable.icon));
		builder.setMessage(msg);
		
		if(negativeBtnText != null){
			builder.setNegativeButton(negativeBtnText, null);
		}
		
		if(positiveBtnText != null && onPositive != null){
			builder.setPositiveButton(positiveBtnText, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					onPositive.run();
				}
			});
		}
		
		builder.create().show();
	}
	
	public boolean isInteger(String str){
		try{
			Integer.parseInt(str);
			return true;
		}catch(NumberFormatException e){}
		
		return false;
	}
	
	public boolean isDouble(String str){
		try{
			Double.parseDouble(str);
			return true;
		}catch(NumberFormatException e){}
		
		return false;
	}
}
