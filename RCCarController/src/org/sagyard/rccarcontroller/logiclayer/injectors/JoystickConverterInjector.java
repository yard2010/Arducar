package org.sagyard.rccarcontroller.logiclayer.injectors;

import org.sagyard.rccarcontroller.logiclayer.implementations.JoystickConverter;

import org.sagyard.rccarcontroller.logiclayer.interfaces.UserInputToVelocity;

public class JoystickConverterInjector implements UserInputToVelocityInjector
{

	@Override
	public UserInputToVelocity getConverter()
	{
		return new JoystickConverter();
	}

}
