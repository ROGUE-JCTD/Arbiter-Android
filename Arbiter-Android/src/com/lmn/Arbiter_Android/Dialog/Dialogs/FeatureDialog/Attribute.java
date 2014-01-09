package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.lmn.Arbiter_Android.Dialog.Dialogs.DateTime.DatePickerFragment;

import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Spinner;

public class Attribute {
	private Spinner spinner;
	private EditText editText;
	private EnumerationHelper enumHelper;
	private SimpleDateFormat formatter;
	private FragmentActivity activity;
	private String dateValue;
	
	private Attribute(FragmentActivity activity, EnumerationHelper enumHelper){
		this.enumHelper = enumHelper;
		this.formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		this.activity = activity;
		this.dateValue = null;
		this.spinner = null;
		this.editText = null;
	}
	
	public Attribute(FragmentActivity activity, Spinner spinner,
			EnumerationHelper enumHelper, boolean startInEditMode){
		
		this(activity, enumHelper);
		
		this.spinner = spinner;
		
		setEditMode(startInEditMode);
	}
	
	public Attribute(FragmentActivity activity, EditText editText, 
			EnumerationHelper enumHelper, boolean startInEditMode, String value){
		
		this(activity, enumHelper);
		
		this.editText = editText;
		
		setStartValue(value);
		
		setEditMode(startInEditMode);
	}
	
	private void toggleSpinner(boolean editMode){
		spinner.setEnabled(editMode);
	}
	
	private void toggleEditText(boolean editMode){
		
		editText.setFocusable(editMode);
		editText.setFocusableInTouchMode(editMode);
	}
	
	private void toggleDate(boolean editMode){
		editText.setEnabled(editMode);
	}
	
	public void setEditMode(boolean editMode){
		if(enumHelper != null && enumHelper.hasEnumeration()){
			
			// Is a spinner
			toggleSpinner(editMode);
		}else{
			
			if(enumHelper != null && (enumHelper.getType().equals("xsd:dateTime")
					|| enumHelper.getType().equals("xsd:date")
					|| enumHelper.getType().equals("xsd:time"))){
				
				toggleDate(editMode);
			}else{
				toggleEditText(editMode);
			}
		}
	}
	
	private String getNow(){
		return formatter.format(Calendar.getInstance().getTime());
	}
	
	private void setStartValue(String value){
		
		if(enumHelper != null && (enumHelper.getType().equals("xsd:dateTime"))){
			
			if(value == null || value.equals("")){
				value = getNow();
			}
			
			this.dateValue = value;
			
			this.setDate(value);
			
			final Attribute attribute = this;
			
			editText.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					DatePickerFragment frag = null;
					
					try {
						frag = DatePickerFragment.newInstance(dateValue, attribute, true);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					frag.show(activity.getSupportFragmentManager(), DatePickerFragment.TAG);
				}
				
			});
			
			editText.setEnabled(false);
			editText.setFocusable(false);
			editText.setFocusableInTouchMode(false);
		}else{
			editText.setText(value);
		}
	}
	
	private String getEditTextValue(){
		return editText.getText().toString();
	}
	
	private String getSpinnerValue(){
		return (String) spinner.getSelectedItem();
	}
	
	private String getDateValue(){
		return dateValue;
	}
	
	public String getValue(){
		
		String value = null;
		
		if(enumHelper != null && enumHelper.hasEnumeration()){
			
			// Is a spinner
			value = getSpinnerValue();
		}else{
			
			if(enumHelper != null && (enumHelper.getType().equals("xsd:dateTime") 
					|| enumHelper.getType().equals("xsd:date")
					|| enumHelper.getType().equals("xsd:time"))){
				value = getDateValue();
			}else{
				value = getEditTextValue();
			}
		}
		
		return value;
	}
	
	public void setDate(String newDate){
		this.dateValue = newDate;
		
		String formattedDate = null;
		
		try {
			formattedDate = (this.formatter.parse(this.dateValue)).toString();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		editText.setText(formattedDate);
	}
}
