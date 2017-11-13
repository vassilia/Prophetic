package com.example.android.personalstatusmonitor;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
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
    Button turnOnButton, turnOffButton, motorFunctionButton, motorFluctuationButton, dyskinesiaButton, freezeOfGaitButton;
    BluetoothAdapter BA;
    ListView lv;
    Logger log;
    private Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotations);

        // turnOnButton.onTouchEvent(MotionEvent me);

        log = Logger.getLogger(this.getClass().getName());

        turnOnButton = (Button) findViewById(R.id.turn_on_bt);
        turnOffButton = (Button) findViewById(R.id.turn_off_bt);
//        openButton = (Button) findViewById(R.id.openButton);
//        sendButton = (Button) findViewById(R.id.sendButton);
//        closeButton = (Button) findViewById(R.id.closeButton);
        myLabel = (TextView) findViewById(R.id.paired_devices_text);

        BA = BluetoothAdapter.getDefaultAdapter();
        lv = (ListView) findViewById(R.id.pairedDevicesListView);
        motorFunctionButton = (Button) findViewById(R.id.motor_function);
        motorFluctuationButton = (Button) findViewById(R.id.motor_fluctuation);
        dyskinesiaButton = (Button) findViewById(R.id.dyskinesia);
        freezeOfGaitButton = (Button) findViewById(R.id.freeze_of_gait);
        boolean btStatus = getIntent().getBooleanExtra("btStatus", false);
        if (btStatus) {
            turnOnButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_on_pressed));
        } else {
            turnOffButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_off_pressed));
        }
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
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_SHORT).show();
            turnOnButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_on_pressed));
            turnOffButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_notpressed));
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_SHORT).show();
            turnOnButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_on_pressed));
            turnOffButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_notpressed));
        }
    }


    public void off(View v) {
        BA.disable();
        Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_SHORT).show();
        turnOnButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_notpressed));
        turnOffButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_off_pressed));
    }

    public void motorFunction(View v) {
        Toast.makeText(getApplicationContext(), "Motor Function annotated", Toast.LENGTH_SHORT).show();
    }

    public void motorFluctuation(View v) {
        Toast.makeText(getApplicationContext(), "Motor Fluctuation annotated", Toast.LENGTH_SHORT).show();


    }

    public void dyskinesia(View v) {
        Toast.makeText(getApplicationContext(), "Dyskinesia annotated", Toast.LENGTH_SHORT).show();


    }

    public void freezeOfGait(View v) {
        Toast.makeText(getApplicationContext(), "Freeze Of Gait annotated", Toast.LENGTH_SHORT).show();

    }


}