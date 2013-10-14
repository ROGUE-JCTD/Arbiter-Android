package com.lmn.Arbiter_Android;

import java.util.ArrayList;

public abstract class ArbiterList<ListType> {
	protected ArrayList<ListType> list;

	public ArbiterList(){
		list = new ArrayList<ListType>();
	}

	public void addItem(ListType item){
		list.add(item);
		onAddItem(item);
	}

	public void removeItem(int itemIndex){
		ListType item = list.get(itemIndex);
		list.remove(itemIndex);
		onRemoveItem(item);
	}

	public ArrayList<ListType> getList(){
		return list;
	}

	public abstract void onAddItem(ListType item);
	public abstract void onRemoveItem(ListType item);
}