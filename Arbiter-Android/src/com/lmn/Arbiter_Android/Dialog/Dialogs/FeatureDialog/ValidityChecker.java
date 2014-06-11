package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class ValidityChecker {
	
	public ValidityChecker(){
		
	}
	
	public void add(Attribute attribute, EditText editText){
		
		setChangeListeners(attribute, editText);
	}
	
	private void setChangeListeners(final Attribute attribute, EditText editText){
		editText.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				
				attribute.updateValidity();
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
}
