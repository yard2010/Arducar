package org.sagyard.rccarcontroller.logiclayer.implementations;

import android.util.Log;

import org.sagyard.rccarcontroller.logiclayer.interfaces.UserInputToVelocity;
import android.graphics.Point;
import android.os.AsyncTask;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import org.sagyard.rccarcontroller.gui.UpdateTextStatus;
import org.sagyard.rccarcontroller.logiclayer.Constants;
import org.sagyard.rccarcontroller.logiclayer.interfaces.BaseClient;

public class DefaultClient extends BaseClient
{
	private Socket client;
	private boolean isConnected;
	private PrintWriter output;
	private UserInputToVelocity converter;

	public DefaultClient(final String dstName, final int dstPort, final UpdateTextStatus updater, final UserInputToVelocity converter)
	{
		this.converter = converter;
		
		AsyncTask.execute(new Runnable()
		{
			@Override
			public void run()
			{
				// Begin by true. If exceptions occur, this will turn false
				isConnected = true;

				try
				{
					client = new Socket(dstName, dstPort);
					output = new PrintWriter(client.getOutputStream());
					
					// Start sending data in intervals
					sendDataIntervals();
				} catch (IOException e)
				{
					isConnected = false;
					e.printStackTrace();
				} catch (Exception e)
				{
					isConnected = false;
					e.printStackTrace();
				}

				updater.getActivity().runOnUiThread(new Runnable()
				{
					public void run()
					{
						updater.updateStatus();
					}
				});
			}
		});
	}

	@Override
	protected void sendVelocities(Point velocities)
	{
		// TODO Change to json string
		new SendMessage().execute("X" + String.valueOf(velocities.x), "Y" + String.valueOf(velocities.y));
	}

	private class SendMessage extends AsyncTask<String, Void, Void>
	{
		@Override
		protected Void doInBackground(String... params)
		{
			// Print all params to prepare for send
			for (String param : params)
			{
				output.print(param);
			}

			// Flush data - send to server
			output.flush();

			return null;
		}
	}

	@Override
	public boolean isConnected()
	{
		return isConnected;
	}

	@Override
	protected void sendDataIntervals()
	{
		AsyncTask.execute(new Runnable()
		{
			@Override
			public void run()
			{
				// Run as long as the connection is up and running
				while (client.isConnected())
				{
					try
					{
						Log.d("ArduCar", converter.getCurrentVelocities().toString());
						
						sendVelocities(converter.getCurrentVelocities());
						Thread.sleep(Constants.DEFAULT_UPDATE_INTERVAL);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		});
	}
}
