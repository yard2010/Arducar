package org.sagyard.rccarcontroller.gui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.VideoView;
import com.zerokol.views.JoystickView;
import com.zerokol.views.JoystickView.OnJoystickMoveListener;
import java.util.HashMap;
import java.util.Map;
import org.sagyard.rccarcontroller.R;
import org.sagyard.rccarcontroller.clientlogic.Constants;
import org.sagyard.rccarcontroller.clientlogic.DefaultClient;
import org.sagyard.rccarcontroller.clientlogic.IClient;
import org.sagyard.rccarcontroller.clientlogic.IServer;
import org.sagyard.rccarcontroller.clientlogic.JoystickConverter;
import org.sagyard.rccarcontroller.clientlogic.UserInputToVelocity;
import org.sagyard.rccarcontroller.gui.SettingsDialog.OnConfirm;

public class MainActivity extends ActionBarActivity implements OnConfirm
{
	private JoystickView joystick;
	private IClient client;
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
		client = new DefaultClient(prefs.getString(Constants.IP_TAG, ""), 9080);
		converter = new JoystickConverter();

		// Referencing also other views
		joystick = (JoystickView) findViewById(R.id.joystickView);
		videoStream = (VideoView) findViewById(R.id.videoStream);
		isConnected = (TextView) findViewById(R.id.isConnected);

		// Default value
		isConnected.setText(client.isConnected() ? R.string.connected : R.string.disconnected);
		isConnected.setTextColor(client.isConnected()
				? getResources().getColor(R.color.green) : getResources().getColor(R.color.red));

		// Event listener that always returns the variation of the angle in degrees, motion power in percentage and direction of movement
		joystick.setOnJoystickMoveListener(new OnJoystickMoveListener()
		{
			@Override
			public void onValueChanged(int angle, int power, int direction)
			{
				Map<String, Object> kwArgs = new HashMap<String, Object>();
				kwArgs.put("angle", angle);
				kwArgs.put("power", power);
				kwArgs.put("direction", direction);

				client.sendVelocities(converter.calcVelocities(kwArgs));
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
		client = new DefaultClient(prefs.getString(Constants.IP_TAG, ""), 9080);
		isConnected.setText(client.isConnected() ? R.string.connected : R.string.disconnected);
		isConnected.setTextColor(client.isConnected()
				? getResources().getColor(R.color.green) : getResources().getColor(R.color.red));
	}
}
