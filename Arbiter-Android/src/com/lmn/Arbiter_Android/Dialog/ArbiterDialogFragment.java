package com.lmn.Arbiter_Android.Dialog;

import com.lmn.Arbiter_Android.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

/**
 * This class creates a dialog for the project wizard
 */
public abstract class ArbiterDialogFragment extends DialogFragment {
	protected String title;
	protected String ok;
	protected String cancel;
	protected int layout;
	protected OnClickListener validatingClickListener = null;
	private AlertDialog myDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		myDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		
		if(validatingClickListener != null) {
			Button positiveButton = myDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	        positiveButton.setOnClickListener(validatingClickListener);
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(getLayout(), null);
		beforeCreateDialog(view);
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setIcon(R.drawable.icon)
			.setTitle(getTitle())
			.setView(view)
			.setPositiveButton(getOk(),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	onPositiveClick();
                        }
                    }
                )
                .setNegativeButton(getCancel(),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	onNegativeClick();
                        }
                    }
                )
                .create();
		
		this.myDialog = dialog;
		this.myDialog.setCanceledOnTouchOutside(false);
		
		return dialog;
	}
	
	public OnClickListener getValidatingClickListener(){
		return this.validatingClickListener;
	}
	
	public void setValidatingClickListener(OnClickListener validatingClickListener){
		this.validatingClickListener = validatingClickListener;
	}
	
	public String getTitle(){
		return this.title;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public String getOk(){
		return this.ok;
	}
	
	public void setOk(String ok){
		this.ok = ok;
	}
	
	public String getCancel(){
		return this.cancel;
	}
	
	public void setCancel(String cancel){
		this.cancel = cancel;
	}
	
	public int getLayout(){
		return this.layout;
	}
	
	public void setLayout(int layout){
		this.layout = layout;
	}
	
	public abstract void beforeCreateDialog(View view);
	public abstract void onPositiveClick();
	public abstract void onNegativeClick();
}
