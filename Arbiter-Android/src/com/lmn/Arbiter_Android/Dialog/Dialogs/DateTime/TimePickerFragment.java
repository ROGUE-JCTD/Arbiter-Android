package com.lmn.Arbiter_Android.Dialog.Dialogs.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog.Attribute;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.EditText;
import android.widget.TimePicker;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.app.Dialog;

public class TimePickerFragment extends DialogFragment implements OnTimeSetListener{
	public static final String TAG = "TimePickerFragment";
	
	private Calendar calendar;
	
	private Attribute attribute;
	private boolean isFirstTimeSet;
	
	public static TimePickerFragment newInstance(int year, int month, int day,
			Calendar calendar, Attribute attribute){
		
		TimePickerFragment frag = new TimePickerFragment();
		
		frag.calendar = calendar;
		
		//frag.year = year;
		//frag.month = month;
		//frag.day = day;
		frag.isFirstTimeSet = true;
		frag.attribute = attribute;
		
		//Log.w(TAG, TAG + " year = " + year + ", month = "
		//		+ month + ", day = " + day);
		
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		
		// Create a new instance of DatePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, hour, minute,
				DateFormat.is24HourFormat(getActivity()));
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		
		if(isFirstTimeSet && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 &&
				isFirstTimeSet && android.os.Build.VERSION.SDK_INT != android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){
			isFirstTimeSet = false;
			return;
		}
		
		// TODO Auto-generated method stub
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		
		//Calendar c = Calendar.getInstance();
		//c.set(year, month, day, hourOfDay, minute);
		
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		
		String datetime = formatter.format(calendar.getTime());
		
		attribute.setDate(datetime);
	}

}
