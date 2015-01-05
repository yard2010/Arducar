package org.sagyard.rccarcontroller.logiclayer.interfaces;

import android.graphics.Point;
import java.util.Map;

public abstract class UserInputToVelocity
{
	public abstract void calcVelocities(Map<String, Object> kwArgs);
	public abstract Point getCurrentVelocities();

	protected volatile Point currentVelocitives;
}
