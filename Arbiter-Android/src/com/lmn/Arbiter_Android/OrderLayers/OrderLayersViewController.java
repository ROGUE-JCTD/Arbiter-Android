package com.lmn.Arbiter_Android.OrderLayers;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ListAdapters.OverlayList;

import android.view.View;
import android.widget.ImageButton;

public class OrderLayersViewController {
	public static final String TAG = "OrderLayersController";
	
	private ImageButton addLayersBtn;
	private ImageButton orderLayersBtn;
	private ImageButton cancelOrderLayersBtn;
	private ImageButton doneOrderingLayersBtn;
	private OverlayList overlayList;
	
	public OrderLayersViewController(ImageButton addLayersBtn, ImageButton orderLayersBtn, 
			ImageButton cancelOrderLayersBtn, ImageButton doneOrderingLayersBtn,
			OverlayList overlayList){
	
		this.addLayersBtn = addLayersBtn;
		this.orderLayersBtn = orderLayersBtn;
		this.cancelOrderLayersBtn = cancelOrderLayersBtn;
		this.doneOrderingLayersBtn = doneOrderingLayersBtn;
		this.overlayList = overlayList;
	}
	
	private void toggleButtons(boolean orderLayersMode){
		if(orderLayersMode){
			this.addLayersBtn.setVisibility(View.GONE);
			this.orderLayersBtn.setVisibility(View.GONE);
			this.cancelOrderLayersBtn.setVisibility(View.VISIBLE);
			this.doneOrderingLayersBtn.setVisibility(View.VISIBLE);
		}else{
			this.addLayersBtn.setVisibility(View.VISIBLE);
			this.orderLayersBtn.setVisibility(View.VISIBLE);
			this.cancelOrderLayersBtn.setVisibility(View.GONE);
			this.doneOrderingLayersBtn.setVisibility(View.GONE);
		}
	}
	
	public void beginOrderLayersMode(){
		boolean orderLayersMode = true;
		
		toggleButtons(orderLayersMode);
		toggleAdapterLayout(orderLayersMode);
	}
	
	public void endOrderLayersMode(){
		boolean orderLayersMode = false;
		
		toggleButtons(orderLayersMode);
		toggleAdapterLayout(orderLayersMode);
	}
	
	private void toggleAdapterLayout(boolean orderLayersMode){
		
		if(orderLayersMode){
			this.overlayList.setItemLayout(R.layout.order_layers_list_item);
		}else{
			this.overlayList.setItemLayout(R.layout.layers_list_item);
		}
		
		this.overlayList.onDataUpdated();
	}
}
