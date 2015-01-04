package org.sagyard.rccarcontroller.clientlogic;

import android.graphics.Point;
import java.util.Map;

public interface UserInputToVelocity
{
	public Point calcVelocities(Map<String, Object> kwArgs);
}
