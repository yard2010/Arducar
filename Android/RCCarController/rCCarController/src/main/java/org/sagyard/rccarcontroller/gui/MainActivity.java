package org.sagyard.rccarcontroller.gui;

import android.net.Uri;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
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
import org.sagyard.rccarcontroller.logiclayer.interfaces.UserInputToVelocity;

public class MainActivity extends Activity implements OnConfirm, UpdateTextStatus {
    private JoystickView joystick;
    private BaseClient client;
    private UserInputToVelocity converter;
    private TextView isConnected;
    private VideoView videoStream;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(Constants.SHARED_PREFS_TAG, Activity.MODE_PRIVATE);

        // Is this the right place to create the "real" implementors?
        converter = new JoystickConverterInjector().getConverter();
        client =
                new DefaultClientInjector().getClient(prefs.getString(Constants.IP_TAG, ""),
                        prefs.getInt(Constants.PORT_TAG, 0),
                        this,
                        converter);

        // Referencing also other views
        joystick = (JoystickView) findViewById(R.id.joystickView);
        videoStream = (VideoView) findViewById(R.id.videoStream);
        isConnected = (TextView) findViewById(R.id.isConnected);

        // Start the stream
        setupVideoStream();

        // Make sure the z index is correct (other views on top of the video stream)
        isConnected.bringToFront();
        joystick.bringToFront();

        // Event listener that always returns the variation of the angle in degrees, motion power in percentage and direction of movement
        joystick.setOnJoystickMoveListener(new OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                Map<String, Object> kwArgs = new HashMap<>();
                kwArgs.put("angle", angle);
                kwArgs.put("power", power);
                kwArgs.put("direction", direction);

                // Log.d("Arducar", String.valueOf(angle));

                // Update the converter
                converter.calcVelocities(kwArgs);
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                SettingsDialog settings = new SettingsDialog();
                settings.show(getFragmentManager(), null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onChoose() {
        client =
                new DefaultClientInjector().getClient(prefs.getString(Constants.IP_TAG, ""),
                        prefs.getInt(Constants.PORT_TAG, 0),
                        this,
                        converter);

        setupVideoStream();
    }

    @Override
    public void updateStatus() {
        isConnected.setText(client.isConnected() ? R.string.connected : R.string.disconnected);
        isConnected.setTextColor(client.isConnected()
                ? getResources().getColor(R.color.green) : getResources().getColor(R.color.red));
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    private void setupVideoStream() {
        try {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoStream);
            mediaController.setMediaPlayer(videoStream);

            // DEBUG for testing purposes
//			videoStream.setVideoURI(Uri.parse("http://www.androidbegin.com/tutorial/AndroidCommercial.3gp"));
            // DEBUG end. Uncomment code bellow

            videoStream.setVideoURI(Uri.parse("http://" + prefs.getString(Constants.IP_TAG, "") + ":" +
                    prefs.getInt(Constants.PORT_TAG, 0) + "/" + Constants.VID_STREAM_URL_TAG + "/"));
            videoStream.setMediaController(mediaController);
            videoStream.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Start video when buffering is complete
                    videoStream.start();
                }
            });
        } catch (Exception e) {
            Log.e("Arducar", e.getMessage());
        }
    }
}