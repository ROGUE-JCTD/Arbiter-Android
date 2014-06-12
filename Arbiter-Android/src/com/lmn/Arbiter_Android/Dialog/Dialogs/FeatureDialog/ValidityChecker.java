package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.HashMap;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;

public class ValidityChecker {
	
	private HashMap<String, Boolean> spinnersInitialized;
	
	public ValidityChecker(){
	
		spinnersInitialized = new HashMap<String, Boolean>();
	}
	
	public void add(final Attribute attribute, EditText editText){
		
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
	
	public void add(final String key, final Attribute attribute, Spinner spinner){
		
		spinnersInitialized.put(key, false);
		
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(spinnersInitialized.get(key)){
					attribute.updateValidity();
				}else{
					spinnersInitialized.put(key, true);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
	}
}
