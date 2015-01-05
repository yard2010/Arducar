package org.sagyard.rccarcontroller.clientlogic;

import android.graphics.Point;
import java.util.Map;

public class JoystickConverter implements UserInputToVelocity
{
	private final float moveSpeed = 100;

	private Point getXYAxis(int angle, int power)
	{
		double y = Math.sin(Math.toRadians(angle));
		double x = Math.cos(Math.toRadians(angle));
		double length = Math.sqrt((x * x) + (y * y));
		x /= length;
		y /= length;

		double currSpeed = moveSpeed * (power / 100.0f);
		x *= currSpeed;
		y *= currSpeed;

		return new Point((int) x, (int) y);
	}

	@Override
	public Point calcVelocities(Map<String, Object> kwArgs)
	{
		int angle = (int) kwArgs.get("angle");
		int power = (int) kwArgs.get("power");
		// int direction = (int) kwArgs.get("direction");

		Point joystickPosition = getXYAxis(angle, power);
		return new Point((int) Constants.clamp(joystickPosition.x,
				Constants.minVelocity,
				Constants.maxVelocity), (int) Constants.clamp(joystickPosition.y,
				Constants.minVelocity,
				Constants.maxVelocity));
	}
}
