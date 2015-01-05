package org.sagyard.rccarcontroller.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import org.sagyard.rccarcontroller.logiclayer.Constants;

public class SettingsDialog extends DialogFragment
{
	private EditText ipAddr;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		final SharedPreferences prefs = getActivity().getSharedPreferences(Constants.SHARED_PREFS_TAG, Activity.MODE_PRIVATE);
		
		// Initializing variables
		ipAddr = new EditText(getActivity());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Set ip addr in view first
		ipAddr.setText(prefs.getString(Constants.IP_TAG, ""));
		
		builder.setTitle("Set IP Address")
				.setView(ipAddr)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						prefs.edit().putString(Constants.IP_TAG, ipAddr.getText().toString()).commit();
						
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