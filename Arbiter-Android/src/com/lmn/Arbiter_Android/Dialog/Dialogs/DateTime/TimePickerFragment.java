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
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.app.Dialog;

public class TimePickerFragment extends DialogFragment implements OnTimeSetListener{
	public static final String TAG = "TimePickerFragment";
	
	private Calendar calendar;
	private String attributeType;
	private Attribute attribute;
	private int offsetFromUTC;
	private SimpleDateFormat sdf;
	private Util util;
	
	public static TimePickerFragment newInstance(Calendar isoCalendar, Attribute attribute, String attributeType, Util util, int offsetFromUTC) throws Exception{
		
		TimePickerFragment frag = new TimePickerFragment();
		
		frag.util = util;
		frag.attribute = attribute;
		frag.attributeType = attributeType;
		frag.offsetFromUTC = offsetFromUTC;
		frag.sdf = util.getSimpleDateFormat(attributeType);
		frag.setCalendar(isoCalendar);
		
		return frag;
	}
	
	private void setCalendar(Calendar isoCalendar) throws Exception{
		
		Date localDate = new Date(isoCalendar.getTimeInMillis() + offsetFromUTC);
		
		String date = null;
		
		if("xsd:time".equals(attributeType)){
			
			String time = sdf.format(localDate);
			
			String now = util.getNow("xsd:dateTime", true);
			
			Log.w(TAG, TAG + "setCalendar now = " + now);
			
			String[] parts = now.split("T");
			
			parts[1] = time;
			
			date = parts[0] + "T" + parts[1];
		}else{
			
			date = sdf.format(localDate);
		}
		
		this.calendar = (new LocalTime(date, true)).getLocalCalendar();
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
		
		Log.w(TAG, TAG + ".onCreate hour = " + hour + " minute = " + minute);
		
		// Create a new instance of DatePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, hour, minute,
				DateFormat.is24HourFormat(getActivity()));
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		
		if(view.isShown()){
			
			calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calendar.set(Calendar.MINUTE, minute);
			
			String datetime = sdf.format(calendar.getTime());
			
			Log.w("TimePicker", "TimePicker: datetime = " + datetime);
			
			try {
				attribute.setDate(calendar);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
