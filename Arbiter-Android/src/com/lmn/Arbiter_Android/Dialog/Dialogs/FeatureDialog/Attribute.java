package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.Dialog.Dialogs.DateTime.DatePickerFragment;

import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
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
	private boolean valid;
	private String startValue;
	
	private Attribute(FragmentActivity activity, EnumerationHelper enumHelper){
		this.enumHelper = enumHelper;
		this.formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		this.activity = activity;
		this.dateValue = null;
		this.spinner = null;
		this.editText = null;
		this.valid = true;
	}
	
	public Attribute(FragmentActivity activity, Spinner spinner,
			EnumerationHelper enumHelper, boolean startInEditMode){
		
		this(activity, enumHelper);
		
		this.spinner = spinner;
		
		this.startValue = getSpinnerValue();
				
		setEditMode(startInEditMode);
	}
	
	public Attribute(FragmentActivity activity, EditText editText, 
			EnumerationHelper enumHelper, boolean startInEditMode, String value){
		
		this(activity, enumHelper);
		
		this.editText = editText;
		
		this.startValue = value;
		
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
	
	public boolean isValid(){
		
		return valid;
	}
	
	public boolean updateValidity(){
		Resources resources = activity.getResources();
		String type = enumHelper.getType();
		String val = editText.getText().toString();
		
		Util util = new Util();
		
		if(val.isEmpty()){
			valid = true;
		}else{
			if(type.equals("xsd:integer") || type.equals("xsd:int")){
				
				valid = util.isInteger(val);
				
				if(!valid){
					editText.setError(resources.getString(
							R.string.form_error_integer));
				}
			}else if(type.equals("xsd:double")){
				
				valid = util.isDouble(val);
				
				if(!valid){
					editText.setError(resources.getString(
							R.string.form_error_double));
				}
			}else if(type.equals("xsd:boolean")){
				
				if(val.equals("true") || val.equals("false")){
					valid = true;
				}else{
					valid = false;
					editText.setError(resources.getString(
							R.string.form_error_bool));
				}
			}else if(type.equals("xsd:dateTime") 
					|| type.equals("xsd:date") 
					|| type.equals("xsd:time")){
				
				try {
					formatter.parse(dateValue);
					valid = true;
				} catch (ParseException e) {
					valid = false;
				}
				
				if(!valid){
					editText.setError(resources.getString(
							R.string.form_error_date));;
				}
			}
		}
		
		return valid;
	}
}
