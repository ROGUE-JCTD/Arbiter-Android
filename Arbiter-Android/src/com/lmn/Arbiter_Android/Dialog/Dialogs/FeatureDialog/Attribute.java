package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.Dialog.Dialogs.DateTime.DatePickerFragment;
import com.lmn.Arbiter_Android.Dialog.Dialogs.DateTime.TimePickerFragment;
import com.lmn.Arbiter_Android.TimeZone.LocalTime;

import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Spinner;

public class Attribute {
	private Spinner spinner;
	private EditText editText;
	private EnumerationHelper enumHelper;
	private FragmentActivity activity;
	private String dateValue;
	private String type;
	private Calendar isoCalendar;
	private Util util;
	private int offsetFromUTC;
	private boolean isNillable;
	
	// Edit text for displaying errors for spinners
	private EditText errorEditText;
	
	private Attribute(FragmentActivity activity, EnumerationHelper enumHelper, boolean isNillable, Util util){
		this.enumHelper = enumHelper;
		
		this.isNillable = isNillable;
		
		if(enumHelper != null){
			this.type = enumHelper.getType();
		}else{
			this.type = null;
		}
		
		this.util = util;
		
		this.offsetFromUTC = util.getOffsetFromUTC();
					
		this.activity = activity;
		this.dateValue = null;
		this.spinner = null;
		this.editText = null;
	}
	
	public Attribute(FragmentActivity activity, Spinner spinner, EditText errorEditText,
			EnumerationHelper enumHelper, boolean isNillable, boolean startInEditMode, Util util){
		
		this(activity, enumHelper, isNillable, util);
		
		this.spinner = spinner;
		this.errorEditText = errorEditText;
		
		setEditMode(startInEditMode);
	}
	
	public Attribute(FragmentActivity activity, EditText editText, 
			EnumerationHelper enumHelper, boolean isNillable, boolean startInEditMode,
			String value, Util util){
		
		this(activity, enumHelper, isNillable, util);
		
		this.editText = editText;
		
		if(isTimeType()){
			
			try{
				
				if(value == null || "".equals(value)){
					value = util.getNow(type, true);
				}
				
				// Add the 'Z' to the end of the string in case it isn't there.
				if(value.charAt(value.length() - 1) != 'Z'){
					value += 'Z';
				}
				
				setCalendar(value);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		try {
			
			setStartValue(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setEditMode(startInEditMode);
	}
	
	private boolean isTimeType(){
		
		return ("xsd:dateTime".equals(type) || "xsd:date".equals(type) || "xsd:time".equals(type));
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
			
			if(type != null && (type.equals("xsd:dateTime")
					|| type.equals("xsd:date")
					|| type.equals("xsd:time"))){
				
				toggleDate(editMode);
			}else{
				toggleEditText(editMode);
			}
		}
	}
	
	private void setCalendar(String value) throws Exception{
		
		Calendar localCalendar = (new LocalTime(util.getNow("xsd:dateTime", true), true)).getLocalCalendar();
		
		SimpleDateFormat formatter = util.getSimpleDateFormat(type);
		Date date = formatter.parse(value);
		
		localCalendar.setTime(date);
		
		LocalTime localTime = new LocalTime(localCalendar);
		
		this.isoCalendar = localTime.getISOCalendar(localCalendar);
	}
	
	private void setStartValue(String value) throws Exception{
		
		if(type != null && isTimeType()){
			
			this.dateValue = value;
			
			this.setDateField(this.dateValue);
			
			final Attribute attribute = this;
			
			if("xsd:dateTime".equals(type)){
				
				editText.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						DatePickerFragment frag = null;
						
						try {
							
							frag = DatePickerFragment.newInstance(isoCalendar, attribute, type, util, offsetFromUTC);
							frag.show(activity.getSupportFragmentManager(), DatePickerFragment.TAG);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				});
			}else if("xsd:time".equals(type)){
				
				editText.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v){
						
						TimePickerFragment frag = null;
						
						try {
							frag = TimePickerFragment.newInstance(isoCalendar, attribute, type, util, offsetFromUTC);
							frag.show(activity.getSupportFragmentManager(), TimePickerFragment.TAG);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}else{ // "xsd:date"
				
				editText.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v){
						
						DatePickerFragment frag = null;
						
						try {
							
							frag = DatePickerFragment.newInstance(isoCalendar, attribute, type, util, offsetFromUTC);
							
							frag.show(activity.getSupportFragmentManager(), TimePickerFragment.TAG);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
			
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
			
			if(type != null && (type.equals("xsd:dateTime") 
					|| type.equals("xsd:date")
					|| type.equals("xsd:time"))){
				value = getDateValue();
			}else{
				value = getEditTextValue().trim();
			}
		}
		
		return value;
	}
	
	private String getDateTimeFromTime(String time) throws Exception{
		
		String now = util.getNow("xsd:dateTime", true);
		
		String[] parts = now.split("T");
		
		parts[1] = time;
		
		return parts[0] + "T" + parts[1];
	}
	
	private String getDateTimeFromDate(String date) throws Exception{
		
		String now = util.getNow("xsd:dateTime", true);
		
		String[] parts = now.split("T");
		
		if(date.charAt(date.length() - 1) == 'Z'){
			date = date.substring(0, date.length() - 1);
		}
		
		parts[0] = date;
		
		return parts[0] + "T" + parts[1];
	}

	// Takes in a GMT date
	public void setDateField(String dateStr) throws Exception{
		
		//Add offset to date
		SimpleDateFormat format = util.getSimpleDateFormat("xsd:dateTime");
		
		if("xsd:time".equals(type)){
			
			dateStr = getDateTimeFromTime(dateStr);
		}else if("xsd:date".equals(type)){
			
			dateStr = getDateTimeFromDate(dateStr);
		}
		
		Date date = format.parse(dateStr);
		
		Date dateWithOffset = new Date(date.getTime() + offsetFromUTC);
		
		Calendar localCalendar = (new LocalTime(format.format(dateWithOffset), true)).getLocalCalendar();
		
		LocalTime localTime = new LocalTime(localCalendar);
		
		Calendar isoCalendar = localTime.getISOCalendar(localCalendar);
		
		editText.setText(util.getHumanReadableDate(isoCalendar, type));
	}
	
	private String getGMTFromLocal(Calendar localCalendar) throws Exception{
		
		Date gmtDate = new Date(localCalendar.getTimeInMillis() - offsetFromUTC);
		
		SimpleDateFormat format = util.getSimpleDateFormat(type);
		
		return format.format(gmtDate);
	}
	
	// Will always get the date + the offset
	public void setDate(Calendar localCalendar) throws Exception{
		
		String gmtValue = getGMTFromLocal(localCalendar);
		
		this.dateValue = gmtValue;
		
		setDateField(this.dateValue);
	}
	
	public boolean updateValidity(){
		Resources resources = activity.getResources();
		
		boolean valid = true;
		
		if(editText != null){
			
			String val = editText.getText().toString();
			
			if(val.trim().isEmpty()){
				Log.w("Attribute", "Attribute its empty valid =  " + isNillable);
				
				valid = isNillable;
				
				if(!valid){
					
					editText.setError(resources.getString(R.string.required_field));
				}
			}else{
				
				if(enumHelper != null){
				
					String type = enumHelper.getType();
					
					if(type.equals("xsd:integer") || type.equals("xsd:int")){
						
						valid = util.isInteger(val.trim());
						
						if(!valid){
							editText.setError(resources.getString(
									R.string.form_error_integer));
						}
					}else if(type.equals("xsd:double") || type.equals("xsd:decimal")){
						
						valid = util.isDouble(val.trim());
						
						if(!valid){
							editText.setError(resources.getString(
									R.string.form_error_double));
						}
					}else if(type.equals("xsd:boolean")){
						
						if(val.trim().equals("true") || val.trim().equals("false")){
							valid = true;
						}else{
							valid = false;
							editText.setError(resources.getString(
									R.string.form_error_bool));
						}
					}else if(type.equals("xsd:long")){
						
						valid = util.isLong(val.trim());
						
						if(!valid){
							editText.setError(resources.getString(R.string.form_error_long));
						}
					}else if(type.equals("xsd:float")){
						
						valid = util.isFloat(val.trim());
						
						if(!valid){
							editText.setError(resources.getString(R.string.form_error_float));
						}
					}else if(type.equals("xsd:dateTime") 
							|| type.equals("xsd:date") 
							|| type.equals("xsd:time")){
						
						try {
							SimpleDateFormat formatter = util.getSimpleDateFormat(type);
							formatter.parse(dateValue);
							valid = true;
						} catch (Exception e) {
							valid = false;
						}
						
						if(!valid){
							editText.setError(resources.getString(
									R.string.form_error_date));;
						}
					}
				}
			}

			//this will remove the error message if the user fixes an invalid field
			if(valid) {
				editText.setError(null);
			}
		}else if(spinner != null){
			
			String val = getSpinnerValue();
			
			if(val.trim().isEmpty()){
				
				valid = isNillable;
				
				if(!valid){
					errorEditText.setError(resources.getString(R.string.required_field));
				}
			}else{
				
				valid = true;
			}
			
			if(valid){
				
				errorEditText.setError(null);
			}
		}
		
		return valid;
	}
}
