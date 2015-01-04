package org.sagyard.rccarcontroller.clientlogic;

import android.graphics.Point;

import android.os.AsyncTask;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class DefaultClient implements IClient
{
	private Socket client;
	private boolean isConnected;

	public DefaultClient(final String dstName, final int dstPort)
	{
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
				} catch (IOException e)
				{
					isConnected = false;
					e.printStackTrace();
				} catch (Exception e)
				{
					isConnected = false;
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void sendVelocities(Point velocities)
	{
		// Send data in the following format: X[-]___Y[-]___ Do I need to add padding?
		new SendMessage().execute("X" + String.valueOf(velocities.x),
				"Y" + String.valueOf(velocities.y));
	}

	private class SendMessage extends AsyncTask<String, Void, Void>
	{
		@Override
		protected Void doInBackground(String... params)
		{
			OutputStream out;
			PrintWriter output;

			try
			{
				out = client.getOutputStream();
				output = new PrintWriter(out);

				// Print all params to prepare for send
				for (String param : params)
				{
					output.print(param);
				}

				// Flush data - send to server
				output.flush();

			} catch (IOException e)
			{
				e.printStackTrace();
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			return null;
		}
	}

	@Override
	public boolean isConnected()
	{
		return isConnected;
	}
}
