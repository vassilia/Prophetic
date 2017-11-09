package com.example.android.personalstatusmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.logging.Logger;

public class Annotations extends AppCompatActivity {

    TextView myLabel;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    InputStream mmInputStream;
//    Thread workerThread;
//    byte[] readBuffer;
//    int readBufferPosition;
//    volatile boolean stopWorker;
    Button turnOnButton, turnOffButton;
    BluetoothAdapter BA;
    ListView lv;
    private Set<BluetoothDevice> pairedDevices;
    Logger log;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotations);


        log = Logger.getLogger(this.getClass().getName());

        turnOnButton = (Button) findViewById(R.id.turn_on_bt);
        turnOffButton = (Button) findViewById(R.id.turn_off_bt);
//        openButton = (Button) findViewById(R.id.openButton);
//        sendButton = (Button) findViewById(R.id.sendButton);
//        closeButton = (Button) findViewById(R.id.closeButton);
        myLabel = (TextView) findViewById(R.id.paired_devices_text);

        BA = BluetoothAdapter.getDefaultAdapter();
        lv = (ListView) findViewById(R.id.pairedDevicesListView);

    }
    void findBT() {
        if (BA == null) {
            myLabel.setText("No bluetooth adapter available");
        }

        if (!BA.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("raspberrypi")) {
                    mmDevice = device;
                    break;
                }
            }
        }
        myLabel.setText("Bluetooth Device Found");
    }
    void openBT() throws IOException {
        if (mmSocket != null && mmSocket.isConnected()) {
            myLabel.setText("Bluetooth connection already open");
        } else {
            try {
                mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 7);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            mmSocket.connect();

            mmInputStream = mmSocket.getInputStream();
            //log.info("Available bytes: " + mmInputStream.available());
            //beginListenForData();
            myLabel.setText("Bluetooth connection Opened");
        }

    }
    public void on(View v) {
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void off(View v) {
        BA.disable();
        Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_LONG).show();
    }
}