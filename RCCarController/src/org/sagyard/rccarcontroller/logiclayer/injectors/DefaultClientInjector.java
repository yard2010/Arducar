package org.sagyard.rccarcontroller.logiclayer.injectors;

import org.sagyard.rccarcontroller.logiclayer.interfaces.UserInputToVelocity;

import org.sagyard.rccarcontroller.gui.UpdateTextStatus;
import org.sagyard.rccarcontroller.logiclayer.implementations.DefaultClient;
import org.sagyard.rccarcontroller.logiclayer.interfaces.BaseClient;

public class DefaultClientInjector implements ClientInjector
{

	@Override
	public BaseClient getClient(String dstName, int dstPort, UpdateTextStatus updater, UserInputToVelocity converter)
	{
		return new DefaultClient(dstName, dstPort, updater, converter);
	}

}
