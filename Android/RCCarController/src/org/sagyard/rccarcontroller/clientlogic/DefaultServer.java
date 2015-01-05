package org.sagyard.rccarcontroller.clientlogic;

import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class DefaultServer implements IServer
{
	private Socket server;
	
	public DefaultServer(final String dstName, final int dstPort)
	{
		RecvMessage recv = new RecvMessage();
		recv.execute(dstName, dstPort);
	}
	
	@Override
	public byte[] getVideoStream()
	{
		return null;
	}
	
	private class RecvMessage extends AsyncTask<Object, Void, Void>
	{
		@Override
		protected Void doInBackground(Object... params)
		{
			try
			{
				server = new Socket((String)params[0], (int)params[1]);
				InputStream input = server.getInputStream();
				
				while (true)
				{
					Log.d("ArduCar", String.valueOf(input.read()));
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}

			return null;
		}
	}
}
