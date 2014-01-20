package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class ValidityChecker {

	private int invalidCount;
	
	public ValidityChecker(){
		
	}
	
	public void add(Attribute attribute, EditText editText){
		
		setChangeListeners(attribute, editText);
	}
	
	private void setChangeListeners(final Attribute attribute, EditText editText){
		editText.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				
				checkValidity(attribute);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private boolean checkValidity(Attribute attribute){
		boolean startingValidity = attribute.isValid();
		
		boolean updatedValidity = attribute.updateValidity();
		
		if(!updatedValidity && startingValidity){
			invalidCount++;
		}else if(updatedValidity && !startingValidity){
			invalidCount--;
		}
		
		return updatedValidity;
	}
	
	public boolean checkFormValidity(){
		return invalidCount == 0;
	}
}
