package org.sagyard.rccarcontroller.gui;

import android.text.InputType;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.sagyard.rccarcontroller.logiclayer.Constants;

public class SettingsDialog extends DialogFragment
{
	private EditText ipAddr;
	private EditText portVal;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		final SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_TAG, Activity.MODE_PRIVATE);
		
		// Initializing variables
		ipAddr = new EditText(getActivity());
		portVal = new EditText(getActivity());
		TextView ipText = new TextView(getActivity());
		TextView portText = new TextView(getActivity());
		LinearLayout layout = new LinearLayout(getActivity());
		
		layout.setOrientation(LinearLayout.VERTICAL);
		portVal.setInputType(InputType.TYPE_CLASS_NUMBER);
//		ipAddr.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Set SP values in view first
		ipAddr.setText(prefs.getString(Constants.IP_TAG, ""));
		portVal.setText(String.valueOf(prefs.getInt(Constants.PORT_TAG, 0)));
		ipText.setText("Set IP Adress");
		portText.setText("Set Port");
		
		// Create the whole layout
		layout.addView(ipText);
		layout.addView(ipAddr);
		layout.addView(portText);
		layout.addView(portVal);
		
		builder.setTitle("Set IP Address and Port")
				.setView(layout)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						prefs.edit().putString(Constants.IP_TAG, ipAddr.getText().toString()).commit();
						prefs.edit().putInt(Constants.PORT_TAG, Integer.parseInt(portVal.getText().toString())).commit();
						
						// Callback to the activity to deal with change
						mListener.onChoose();
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						dismiss();
					}
				});

		return builder.create();
	}
	
	public static interface OnConfirm {
	    public void onChoose();
	}

	private OnConfirm mListener;

	@Override
	public void onAttach(Activity activity) {
	    mListener = (OnConfirm) activity;
	    super.onAttach(activity);
	}

	@Override
	public void onDetach() {
	    mListener = null;
	    super.onDetach();
	}
}