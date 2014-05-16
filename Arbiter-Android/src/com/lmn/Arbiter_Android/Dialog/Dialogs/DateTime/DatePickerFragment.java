package com.lmn.Arbiter_Android.Dialog.Dialogs.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog.Attribute;
import com.lmn.Arbiter_Android.TimeZone.LocalTime;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.DatePicker;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;

public class DatePickerFragment extends DialogFragment implements  OnDateSetListener{
	public static final String TAG = "DatePickerFragment";
	
	private SimpleDateFormat sdf;
	private Calendar calendar;
	private String attributeType;
	private Attribute attribute;
	private Util util;
	private int offsetFromUTC;
	
	public static DatePickerFragment newInstance(Calendar isoCalendar, Attribute attribute, String attributeType, Util util, int offsetFromUTC) throws Exception{
		DatePickerFragment frag = new DatePickerFragment();
		
		frag.util = util;
		
		frag.sdf = util.getSimpleDateFormat(attributeType);
		
		frag.attribute = attribute;
		
		frag.attributeType = attributeType;
		
		frag.offsetFromUTC = offsetFromUTC;
		
		frag.setCalendar(isoCalendar);
		
		return frag;
	}
	
	private void setCalendar(Calendar isoCalendar) throws ParseException{
		
		Log.w(TAG, TAG + ".setCalendar attributeType = " + attributeType);
		
		if("xsd:date".equals(attributeType)){
			calendar = isoCalendar;
		}else{
			Date localDate = new Date(isoCalendar.getTimeInMillis() + offsetFromUTC);
			
			this.calendar = (new LocalTime(sdf.format(localDate), ("xsd:dateTime".equals(attributeType)))).getLocalCalendar();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		Log.w("DatePickerFragment", "DatePickerFragment year = "
				+ year + ", month = " + month + ", day = " + day);
		
		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		
		if(view.isShown()){
			
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, monthOfYear);
			calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			
			if(attributeType.equals("xsd:dateTime")){
				
				Fragment timePicker = getActivity().getSupportFragmentManager().findFragmentByTag(TimePickerFragment.TAG);
				
				if(timePicker == null){
					
					try {
						timePicker = TimePickerFragment.newInstance(calendar, attribute, attributeType, util, offsetFromUTC);
						
						((TimePickerFragment)timePicker).show(getActivity().getSupportFragmentManager(), TimePickerFragment.TAG);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else{
				setField();
			}
		}
	}
	
	private void setField(){
		
		try {
			attribute.setDate(calendar);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
