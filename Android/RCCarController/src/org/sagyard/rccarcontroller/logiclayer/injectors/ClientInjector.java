package org.sagyard.rccarcontroller.logiclayer.injectors;

import org.sagyard.rccarcontroller.logiclayer.interfaces.UserInputToVelocity;

import org.sagyard.rccarcontroller.gui.UpdateTextStatus;
import org.sagyard.rccarcontroller.logiclayer.interfaces.BaseClient;

public interface ClientInjector
{
	public BaseClient getClient(String dstName, int dstPort, UpdateTextStatus updater, UserInputToVelocity converter);
}
