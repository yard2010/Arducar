package org.sagyard.rccarcontroller.clientlogic;

import android.graphics.Point;

public interface IClient
{
	public void sendVelocities(Point velocities);
	public boolean isConnected();
}
