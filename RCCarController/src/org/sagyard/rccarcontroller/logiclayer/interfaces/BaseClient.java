package org.sagyard.rccarcontroller.logiclayer.interfaces;

import android.graphics.Point;

public abstract class BaseClient
{
	public abstract boolean isConnected();
	protected abstract void sendVelocities(Point velocities);
	protected abstract void sendDataIntervals();
}
