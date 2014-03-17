package com.lmn.Arbiter_Android.ListAdapters;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChooseGeometryTypeAdapter extends BaseAdapter{

	private MapChangeListener mapChangeListener;
	
	public static class Mode{
		public static final int INSERT = 0;
		public static final int ADD_PART = 1;
	}
	
	private String[] geometryTypes = {
		"Point",
		"LineString",
		"Polygon",
		"MultiPoint",
		"MultiLineString",
		"MultiPolygon"
	};
	
	private final LayoutInflater inflater;
	private int itemLayout;
	private Context context;
	private DialogFragment dialog;
	private String featureType;
	private long layerId;
	private int mode;
	
	public ChooseGeometryTypeAdapter(DialogFragment dialog,
			int itemLayout, String featureType, long layerId, int mode){
		
		this.context = dialog.getActivity().getApplicationContext();
		this.inflater = LayoutInflater.from(this.context);
		
		this.itemLayout = itemLayout;
		this.dialog = dialog;
		
		this.featureType = featureType;
		this.layerId = layerId;
		this.mode = mode;
		
		try {
			mapChangeListener = (MapChangeListener) dialog.getActivity();
		} catch (ClassCastException e){
			throw new ClassCastException(dialog.getActivity().toString() 
					+ " must implement MapChangeListener");
		}
	}
	
	private String getLocalizedGeometryType(int position){
		
		String geometryType = null;
		Resources resources = context.getResources();
		
		switch(position){
			case 0:
				geometryType = resources.getString(R.string.point);
				
				break;
				
			case 1:
				geometryType = resources.getString(R.string.line);
				
				break;
				
			case 2:
				geometryType = resources.getString(R.string.polygon);
				
				break;
				
			case 3:
				geometryType = resources.getString(R.string.multipoint);
				
				break;
				
			case 4:
				geometryType = resources.getString(R.string.multiline);
				
				break;
				
			case 5: 
				geometryType = resources.getString(R.string.multipolygon);
				
				break;
				
			default:
			
		}
		
		return geometryType;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		// Inflate the layout
		if(view == null){
			view = inflater.inflate(itemLayout, null);
		}
		
		final String geometryType = getItem(position);
		
        TextView geometryTypeTextView = (TextView) view.findViewById(R.id.geometryType);
        
        if(geometryTypeTextView != null){
        	geometryTypeTextView.setText(getLocalizedGeometryType(position));
        }
		
		view.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				
				if(mode == Mode.INSERT){
					Log.w("ChooseGeometry", "ChooseGeometry insert mode");
					mapChangeListener.getMapChangeHelper()
						.startInsertMode(featureType, layerId, geometryType);
				}else if(mode == Mode.ADD_PART){
					
					Log.w("ChooseGeometry", "ChooseGeometry add part mode");
					mapChangeListener.getMapChangeHelper().startAddPartMode(geometryType);
				}
				
				dialog.dismiss();
			}
		});
		
		return view;
	}
	
	@Override
	public int getCount() {
		if(geometryTypes == null){
			return 0;
		}
		
		return geometryTypes.length;
	}

	@Override
	public String getItem(int position) {
		return geometryTypes[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
