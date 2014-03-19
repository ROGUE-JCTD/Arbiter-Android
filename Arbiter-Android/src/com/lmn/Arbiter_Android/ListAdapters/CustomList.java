package com.lmn.Arbiter_Android.ListAdapters;

import android.view.View;
import android.view.ViewGroup;

public abstract class CustomList<Container, Item> implements ArbiterAdapter<Container>{

	private Container data;
	private ViewGroup viewGroup;
	
	public CustomList(ViewGroup viewGroup){
		this.viewGroup = viewGroup;
		this.data = null;
	}
	
	public void setData(Container data){
		this.data = data;
		
		onDataUpdated();
	}
	
	private void empty(){
		
		this.viewGroup.removeAllViews();
	}
	
	private void populate(){
		
		View view = null;
		
		for(int i = 0, count = getCount(); i < count; i++){
			
			view = getView(i);
			
			viewGroup.addView(view);
		}
	}
	
	public void onDataUpdated(){
		
		empty();
		
		if(this.data != null){
			
			populate();
		}
	}
	
	public Container getData(){
		return data;
	}
	
	public abstract int getCount();
	public abstract Item getItem(int index);
	public abstract View getView(int index);
}
