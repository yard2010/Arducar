package org.sagyard.rccarcontroller.gui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.VideoView;
import com.zerokol.views.JoystickView;
import com.zerokol.views.JoystickView.OnJoystickMoveListener;
import java.util.HashMap;
import java.util.Map;
import org.sagyard.rccarcontroller.R;
import org.sagyard.rccarcontroller.gui.SettingsDialog.OnConfirm;
import org.sagyard.rccarcontroller.logiclayer.Constants;
import org.sagyard.rccarcontroller.logiclayer.injectors.DefaultClientInjector;
import org.sagyard.rccarcontroller.logiclayer.injectors.JoystickConverterInjector;
import org.sagyard.rccarcontroller.logiclayer.interfaces.BaseClient;
import org.sagyard.rccarcontroller.logiclayer.interfaces.IServer;
import org.sagyard.rccarcontroller.logiclayer.interfaces.UserInputToVelocity;

public class MainActivity extends Activity implements OnConfirm, UpdateTextStatus
{
	private JoystickView joystick;
	private BaseClient client;
	private IServer server;
	private UserInputToVelocity converter;
	private TextView isConnected;
	private VideoView videoStream;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		prefs = getSharedPreferences(Constants.SHARED_PREFS_TAG, Activity.MODE_PRIVATE);
		// Is this the right place to create the "real" implementors?
		// server = new DefaultServer(prefs.getString(Constants.IP_TAG, ""), 9080);
		converter = new JoystickConverterInjector().getConverter();
		client =
				new DefaultClientInjector().getClient(prefs.getString(Constants.IP_TAG, ""),
						9080,
						this,
						converter);
		// Referencing also other views
		joystick = (JoystickView) findViewById(R.id.joystickView);
		videoStream = (VideoView) findViewById(R.id.videoStream);
		isConnected = (TextView) findViewById(R.id.isConnected);
		// Event listener that always returns the variation of the angle in degrees, motion power in percentage and direction of movement
		joystick.setOnJoystickMoveListener(new OnJoystickMoveListener()
		{
			// TODO Send data in intervals, get values from variables
			@Override
			public void onValueChanged(int angle, int power, int direction)
			{
				Map<String, Object> kwArgs = new HashMap<String, Object>();
				kwArgs.put("angle", angle);
				kwArgs.put("power", power);
				kwArgs.put("direction", direction);
				// Update the converter
				converter.calcVelocities(kwArgs);
				// client.sendVelocities();
			}
		}, JoystickView.DEFAULT_LOOP_INTERVAL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle presses on the action bar items
		switch (item.getItemId())
		{
		case R.id.action_settings:
			SettingsDialog settings = new SettingsDialog();
			settings.show(getFragmentManager(), null);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onChoose()
	{
		client =
				new DefaultClientInjector().getClient(prefs.getString(Constants.IP_TAG, ""),
						9080,
						this,
						converter);
	}

	@Override
	public void updateStatus()
	{
		isConnected.setText(client.isConnected() ? R.string.connected : R.string.disconnected);
		isConnected.setTextColor(client.isConnected()
				? getResources().getColor(R.color.green) : getResources().getColor(R.color.red));
	}

	@Override
	public Activity getActivity()
	{
		return this;
	}
}