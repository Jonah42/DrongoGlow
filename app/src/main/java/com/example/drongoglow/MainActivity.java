package com.example.drongoglow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    private static final UUID HC05_UUID_SECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID HC05_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter = null;
    public BluetoothDevice bluetoothDevice;
    public BluetoothSocket bluetoothSocket = null;
    private ConnectedThread connected;
    private static final String TAG = "DRONGO-FLOW-MESSAGE";
    private int red = 255;
    private int green = 255;
    private int blue = 255;
    private int solidPrimary = 0xFF;
    private int pulsePeriod = 100;
    private int pulsePrimary = 0xFF;
    private int pulseSecondary = 0x00;
    private int rotatePrimary = 0xFF;
    private int rotateSecondary = 0x00;
    private int rotateSpeed = 100;
    private int rotateWidth = 25;
    private int volumePrimary = 0xFF;
    private int frequencyPrimary = 0xFF;

    private boolean ack = true;
    private boolean ready = false;


    // Constants that indicate the current connection state
//    public static final int STATE_NONE = 0;       // we're doing nothing
//    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
//    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
//    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
//    private final Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(myToolbar);
        // Config Solid picker
        ColorPickerView solidPicker = (ColorPickerView) findViewById(R.id.color_picker_view);
        solidPicker.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int i) {
//                connected.write(("00"+getRGBString(Color.red(i), Color.green(i), Color.blue(i))).getBytes());
            }
        });
        solidPicker.addOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int i) {
                solidPrimary = i;
                connected.write(("00"+getRGBString(Color.red(i), Color.green(i), Color.blue(i))).getBytes());
            }
        });
        // Config pulse period seekbar
        SeekBar pulseSeek = (SeekBar) findViewById(R.id.pulse_seekBar); // initiate the Seekbar
        pulseSeek.setMax(255);
        pulseSeek.setProgress(pulsePeriod);
        pulseSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                pulsePeriod = progress;
                Log.d(TAG, "pulseperiod is now: "+getStringFromByte(pulsePeriod));
                connected.write(("13"+getStringFromByte(pulsePeriod)).getBytes());
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        // Config rotate seekbars
        SeekBar rotateSpeedSeek = (SeekBar) findViewById(R.id.speed_seekBar); // initiate the Seekbar
        rotateSpeedSeek.setMax(255);
        rotateSpeedSeek.setProgress(rotateSpeed);
        rotateSpeedSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                rotateSpeed = progress;
//                Log.d(TAG, "pulseperiod is now: "+getStringFromByte(pulsePeriod));
                connected.write(("23"+getStringFromByte(rotateSpeed)).getBytes());
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        SeekBar rotateWidthSeek = (SeekBar) findViewById(R.id.width_seekBar); // initiate the Seekbar
        rotateWidthSeek.setMax(100);
        rotateWidthSeek.setProgress(rotateWidth);
        rotateWidthSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                rotateWidth = progress;
//                Log.d(TAG, "pulseperiod is now: "+getStringFromByte(pulsePeriod));
                connected.write(("24"+getStringFromByte(rotateWidth)).getBytes());
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        // Config volume picker
        ColorPickerView volumePicker = (ColorPickerView) findViewById(R.id.volume_color_picker_view);
        volumePicker.setInitialColor(0xFF0000FF, false);
        volumePicker.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int i) {
//                connected.write(("00"+getRGBString(Color.red(i), Color.green(i), Color.blue(i))).getBytes());
            }
        });
        volumePicker.addOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int i) {
                volumePrimary = i;
                connected.write(("30"+getRGBString(i)).getBytes());
            }
        });
        // Config frequency picker
        ColorPickerView frequencyPicker = (ColorPickerView) findViewById(R.id.frequency_color_picker_view);
        frequencyPicker.setInitialColor(0xFF0000FF, false);
        frequencyPicker.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int i) {
//                connected.write(("00"+getRGBString(Color.red(i), Color.green(i), Color.blue(i))).getBytes());
            }
        });
        frequencyPicker.addOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int i) {
                frequencyPrimary = i;
                connected.write(("40"+getRGBString(i)).getBytes());
            }
        });
        /*SeekBar greenSeek = (SeekBar) findViewById(R.id.green_seek); // initiate the Seekbar
        greenSeek.setMax(255);
        greenSeek.setProgress(0);
        greenSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            TextView greenValue = (TextView) findViewById(R.id.green_value);
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                greenValue.setText(Integer.toString(progress));
                green = progress;
                String rgb = getRGBString(red, green, blue);
                TextView sent_text = (TextView) findViewById(R.id.sent_text);
                sent_text.setText(rgb);
                connected.write((rgb).getBytes());
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        SeekBar blueSeek = (SeekBar) findViewById(R.id.blue_seek); // initiate the Seekbar
        blueSeek.setMax(255);
        blueSeek.setProgress(0);
        blueSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            TextView blueValue = (TextView) findViewById(R.id.blue_value);
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                blueValue.setText(Integer.toString(progress));
                blue = progress;
                String rgb = getRGBString(red, green, blue);
                TextView sent_text = (TextView) findViewById(R.id.sent_text);
                sent_text.setText(rgb);
                connected.write((rgb).getBytes());
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });*/
    }

    String getRGBString(int r, int g, int b) {
        String ret = "";
        ret += getStringFromByte(r);
        ret += getStringFromByte(g);
        ret += getStringFromByte(b);
        return ret;
    }

    String getRGBString(int c) {
        String ret = "";
        ret += getStringFromByte(Color.red(c));
        ret += getStringFromByte(Color.green(c));
        ret += getStringFromByte(Color.blue(c));
        return ret;
    }

    String getStringFromByte(int x) {
        if (x < 100) return ((x < 10) ? "00"+Integer.toString(x&0xFF) : "0"+Integer.toString(x&0xFF));
        else return Integer.toString(x&0xFF);
    }

    @Override
    public void onStart() {
        super.onStart();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        SubMenu subm = menu.getItem(0).getSubMenu(); // get my MenuItem with placeholder submenu
        subm.clear();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Set<BluetoothDevice> pairedDevices;
        switch (item.getItemId()) {
            case R.id.action_connect:
                // User chose the "Settings" item, show the app settings UI...
//                item.openOptionsMenu();
                pairedDevices = bluetoothAdapter.getBondedDevices();
                SubMenu subm = item.getSubMenu();
                subm.clear();
                int bluetoothIdOffset = 100;
                int index = 0;
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    subm.add(0, bluetoothIdOffset+index, index, deviceName);
                    index++;
                }
                return true;


            default:
                pairedDevices = bluetoothAdapter.getBondedDevices();
                if (item.getItemId() >= 100 && item.getItemId() < 100+pairedDevices.size()) {
                    // Submenu item
                    String title = item.getTitle().toString();
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address
                        if (title.equals(deviceName) && title.equals("HC-05")) { // TODO(Jonah): remove 2nd condition when I rename the device
                            bluetoothDevice = device;
                            ConnectThread connectThread = new ConnectThread(bluetoothDevice);
                            connectThread.start();
                            while (bluetoothSocket == null) {}
                            Log.d("TESTING", "Connected to shit");
                            connected = new ConnectedThread(bluetoothSocket);
                            connected.start();
                            return true;
                        }
                    }
                }
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        // Get message
        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        String message = editText.getText().toString();
        // Set textview
        TextView sent_text = (TextView) findViewById(R.id.sent_text);
        sent_text.setText(message);
        // Send via bluetooth
        connected.write(message.getBytes());
    }

    public void showSolid(View view) {
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.vf);
        vf.setDisplayedChild(0);
        connected.write(("00"+getRGBString(solidPrimary)).getBytes());
    }

    public void showPulse(View view) {
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.vf);
        vf.setDisplayedChild(1);
        connected.write(("10"+getRGBString(pulsePrimary)+getRGBString(pulseSecondary)+getStringFromByte(pulsePeriod)).getBytes());
    }
    public void showRotate(View view) {
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.vf);
        vf.setDisplayedChild(2);
        Log.d(TAG, "Sending: "+"20"+getRGBString(rotatePrimary)+getRGBString(rotateSecondary)+getStringFromByte(rotateSpeed)+getStringFromByte(rotateWidth));
        connected.write(("20"+getRGBString(rotatePrimary)+getRGBString(rotateSecondary)+getStringFromByte(rotateSpeed)+getStringFromByte(rotateWidth)).getBytes());
    }
    public void showVolume(View view) {
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.vf);
        vf.setDisplayedChild(3);
        connected.write(("30"+getRGBString(volumePrimary)).getBytes());
    }
    public void showFrequency(View view) {
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.vf);
        vf.setDisplayedChild(4);
        connected.write(("40"+getRGBString(frequencyPrimary)).getBytes());
    }
    public void showCustom(View view) {
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.vf);
        vf.setDisplayedChild(5);
    }

    public void showPicker(View view) {
        Button b = (Button) view;
        ColorPickerDialogBuilder
            .with(this)
            .setTitle("Choose color")
            .initialColor(Color.rgb(0,0,255))
            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
            .density(12)
            .showAlphaSlider(false)
            .setOnColorSelectedListener(new OnColorSelectedListener() {
                @Override
                public void onColorSelected(int selectedColor) {
                    Log.d(TAG, "onColorSelected: 0x" + Integer.toHexString(selectedColor));
                    if (view.getId() == R.id.select_primary) {
                        pulsePrimary = selectedColor;
                        connected.write(("11"+getRGBString(selectedColor)).getBytes());
                    } else if (view.getId() == R.id.select_secondary) {
                        pulseSecondary = selectedColor;
                        connected.write(("12"+getRGBString(selectedColor)).getBytes());
                    } else if (view.getId() == R.id.rotate_select_primary) {
                        rotatePrimary = selectedColor;
                        connected.write(("21"+getRGBString(selectedColor)).getBytes());
                    } else if (view.getId() == R.id.rotate_select_secondary) {
                        rotateSecondary = selectedColor;
                        connected.write(("22"+getRGBString(selectedColor)).getBytes());
                    }
                }
            })
            .setPositiveButton("ok", new ColorPickerClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                    Log.d(TAG, "onColorSelected: 0x" + Integer.toHexString(selectedColor));
//                    b.setBackgroundColor(selectedColor);
                    Drawable buttonDrawable = b.getBackground();
                    buttonDrawable = DrawableCompat.wrap(buttonDrawable);
                    //the color is a direct color int and not a color resource
                    DrawableCompat.setTint(buttonDrawable, selectedColor);
                    b.setBackground(buttonDrawable);
                    if (view.getId() == R.id.select_primary) {
                        pulsePrimary = selectedColor;
                        connected.write(("11"+getRGBString(selectedColor)).getBytes());
                    } else if (view.getId() == R.id.select_secondary) {
                        pulseSecondary = selectedColor;
                        connected.write(("12"+getRGBString(selectedColor)).getBytes());
                    }
                }
            })
            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            })
            .build()
            .show();
    }

    public void displayMessage(String recv) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                TextView receivedText = (TextView) findViewById(R.id.received_text);
                // Set textview
                receivedText.setText(recv);

            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
//            case REQUEST_CONNECT_DEVICE_SECURE:
//                // When DeviceListActivity returns with a device to connect
//                if (resultCode == Activity.RESULT_OK) {
//                    connectDevice(data, true);
//                }
//                break;
//            case REQUEST_CONNECT_DEVICE_INSECURE:
//                // When DeviceListActivity returns with a device to connect
//                if (resultCode == Activity.RESULT_OK) {
//                    connectDevice(data, false);
//                }
//                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
//                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    finish();
                }
        }
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * param device The BluetoothDevice that has been connected
     */

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(HC05_UUID_SECURE );
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            bluetoothSocket = mmSocket;
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
//                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream
        private byte[] queue = null; // mmBuffer store for the stream
        String received = "";

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    if (numBytes > 0) {
                        received += new String(mmBuffer, 0, numBytes);
                        if (mmBuffer[numBytes-1] == '\n') {
                            displayMessage(received);
                            Log.d(TAG, "Received this: "+received);
                            if (received.startsWith("ACK9")) {
                                Log.d(TAG, "received ACK9");
                                ready = true;
                                if (queue != null) {
                                    Log.d(TAG, "writing from q");
                                    write(queue.clone());
                                    queue = null;
                                }
                            } else if (received.startsWith("ACK")) {
                                Log.d(TAG, "received ACK");
                                ack = true;
                            }
                            received = "";
                        }
                        mmBuffer = new byte[1024];
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            if (ready) {
                ready = false;
                try {
                    mmOutStream.write(bytes);
                    Log.d(TAG, "sending from q");
                } catch (IOException e) {
                    ack = true;
                    Log.e(TAG, "Error occurred when sending data", e);
                }
            }
            Log.d(TAG, "adding to q");
            queue = bytes.clone();
            if (ack) {
                try {
                    ack = false;
                    mmOutStream.write("9".getBytes());
                    Log.d(TAG, "sending ready");
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when sending data", e);
                }
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }


}