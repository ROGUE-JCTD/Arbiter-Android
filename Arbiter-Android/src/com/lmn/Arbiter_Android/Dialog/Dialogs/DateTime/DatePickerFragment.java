package com.lmn.Arbiter_Android.Dialog.Dialogs.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog.Attribute;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;

public class DatePickerFragment extends DialogFragment implements  OnDateSetListener{
	public static final String TAG = "DatePickerFragment";
	
	private SimpleDateFormat sdf;
	private Calendar calendar;
	private boolean isFirstTimeSet;
	private boolean setTime;
	private Attribute attribute;
	
	public static DatePickerFragment newInstance(String dateTimeStr, Attribute attribute, boolean setTime) throws ParseException{
		DatePickerFragment frag = new DatePickerFragment();
		
		frag.sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		
		Date date = frag.sdf.parse(dateTimeStr);

		frag.calendar = Calendar.getInstance();
		
		frag.calendar.setTime(date);
		
		frag.isFirstTimeSet = true;
		
		frag.attribute = attribute;
		
		frag.setTime = setTime;
		
		return frag;
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
		if(isFirstTimeSet && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1){
			isFirstTimeSet = false;
			return;
		}
		
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, monthOfYear);
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		
		if(setTime){
			// Plus 1 to adjust for difference between joda and java.util.date
			TimePickerFragment timePicker = TimePickerFragment.newInstance(year, 
					monthOfYear, dayOfMonth, calendar, attribute);
			
			timePicker.show(getActivity().getSupportFragmentManager(), TimePickerFragment.TAG);
		}else{
			setField(year, monthOfYear, dayOfMonth);
		}
	}
	
	private void setField(int year, int monthOfYear, int dayOfMonth){
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, monthOfYear);
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		
		String newDate = sdf.format(calendar.getTime());
		
		attribute.setDate(newDate);
	}

}
