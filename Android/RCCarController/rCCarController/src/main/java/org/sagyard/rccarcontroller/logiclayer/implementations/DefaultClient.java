package org.sagyard.rccarcontroller.logiclayer.implementations;

import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.sagyard.rccarcontroller.gui.UpdateTextStatus;
import org.sagyard.rccarcontroller.logiclayer.Constants;
import org.sagyard.rccarcontroller.logiclayer.interfaces.BaseClient;
import org.sagyard.rccarcontroller.logiclayer.interfaces.UserInputToVelocity;

public class DefaultClient extends BaseClient {
    private Socket client;
    private PrintWriter output;
    private boolean forceClose;
    private AsyncTask sender;
    private UserInputToVelocity converter;
    private static DefaultClient instance;

    private DefaultClient() {
    }

    public static DefaultClient getInstance() {
        if (instance == null) {
            instance = new DefaultClient();
        }

        return instance;
    }

    public void connect(final String dstName, final int dstPort, final UpdateTextStatus updater, final UserInputToVelocity converter) {
        this.converter = converter;

        // Attempt to kill thread and wait for it to finish
        if (sender != null && sender.getStatus() == AsyncTask.Status.RUNNING) {
            forceClose = true;
            sender.cancel(true);
        }

        sender = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                // Default values
                forceClose = false;

                // Notify connection status before anything is started
                updater.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        updater.updateStatus();
                    }
                });

                // Run as long as this task isn't forced to die
                while (!forceClose) {
                    try {
                        // Close socket if it's open before anything else
                        if (client != null && client.isConnected()) {
                            client.close();
                        }

                        client = new Socket(dstName, dstPort);
                        output = new PrintWriter(client.getOutputStream());

                        // Notify connection status before beginning to loop. Should be good to go
                        // by this point
                        updater.getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                updater.updateStatus();
                            }
                        });

                        // Start sending intervals. Should be stuck in this loop
                        // as long as communication with other devices is working
                        sendDataIntervals();

                        // Notify connection status after data sending is over
                        updater.getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                updater.updateStatus();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }
        };

        // To avoid method calling ambiguity
        sender.execute(new Object());
    }

    @Override
    protected void sendVelocities(Point velocities) {
        String xVel = "X" + String.valueOf(velocities.x);
        String yVel = "Y" + String.valueOf(velocities.y);

        new SendMessage(xVel, yVel).start();
    }

    private class SendMessage extends Thread implements Runnable {
        String[] params;

        public SendMessage(String... params) {
            this.params = params;
        }

        @Override
        public void run() {
            // Print all params to prepare for send
            for (String param : params) {
                output.print(param);
            }

            // Flush data - send to server
            output.flush();
        }
    }

    @Override
    public boolean isConnected() {
        return !forceClose && output != null && !output.checkError();
    }

    @Override
    protected void sendDataIntervals() {
        // Run as long as the connection is up and running
        while (!forceClose && !output.checkError()) {
            try {
//				Log.d("ArduCar", converter.getCurrentVelocities().toString());
                sendVelocities(converter.getCurrentVelocities());
                Thread.sleep(Constants.DEFAULT_UPDATE_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
