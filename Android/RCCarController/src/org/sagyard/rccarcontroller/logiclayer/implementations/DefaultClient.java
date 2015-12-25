package org.sagyard.rccarcontroller.logiclayer.implementations;

import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import org.sagyard.rccarcontroller.gui.UpdateTextStatus;
import org.sagyard.rccarcontroller.logiclayer.Constants;
import org.sagyard.rccarcontroller.logiclayer.interfaces.BaseClient;
import org.sagyard.rccarcontroller.logiclayer.interfaces.UserInputToVelocity;

public class DefaultClient extends BaseClient
{
	private Socket client;
	private boolean isConnected;
	private PrintWriter output;
	private UserInputToVelocity converter;
	private static DefaultClient instance;
	
	private DefaultClient() {}
	
	public static DefaultClient getInstance()
	{
		if (instance == null) {
			instance = new DefaultClient();
		}
		
		return instance;
	}
	
	public void connect(final String dstName, final int dstPort, final UpdateTextStatus updater, final UserInputToVelocity converter)
	{
		this.converter = converter;
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// Begin by true. If exceptions occur, this will turn false
				isConnected = true;

				try
				{
					// Close first if open
					if (client != null && client.isConnected())
					{
						client.close();
					}
					
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
		}).start();
	}

	@Override
	protected void sendVelocities(Point velocities)
	{
		String xVel = "X" + String.valueOf(velocities.x);
		String yVel = "Y" + String.valueOf(velocities.y);
		
		new SendMessage(xVel, yVel).start();
	}

	private class SendMessage extends Thread implements Runnable
	{
		String[] params;
		
		public SendMessage(String... params) {
			this.params = params;
		}
		
		@Override
		public void run()
		{
			// Print all params to prepare for send
			for (String param : params)
			{
				output.print(param);
			}

			// Flush data - send to server
			output.flush();
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
//						Log.d("ArduCar", converter.getCurrentVelocities().toString());
						
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
