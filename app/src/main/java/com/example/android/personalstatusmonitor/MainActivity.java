package com.example.android.personalstatusmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {
    TextView myLabel;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    Button turnOnButton, getVisibleButton, listDeviceButton, turnOffButton, openButton, sendButton, closeButton;
    BluetoothAdapter BA;
    ListView lv;
    private Set<BluetoothDevice> pairedDevices;
    Logger log;



    //go to annotations activity
    private Button mBtLaunchAnnotActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        log = Logger.getLogger(this.getClass().getName());

        turnOnButton = (Button) findViewById(R.id.turn_on_bt);
        getVisibleButton = (Button) findViewById(R.id.get_visible);
        listDeviceButton = (Button) findViewById(R.id.list_devices);
        turnOffButton = (Button) findViewById(R.id.turn_off_bt);
//        openButton = (Button) findViewById(R.id.openButton);
//        sendButton = (Button) findViewById(R.id.sendButton);
//        closeButton = (Button) findViewById(R.id.closeButton);
        myLabel = (TextView) findViewById(R.id.paired_devices_text);

        BA = BluetoothAdapter.getDefaultAdapter();
        lv = (ListView) findViewById(R.id.pairedDevicesListView);

        mBtLaunchAnnotActivity = (Button) findViewById(R.id.connect_bt);
        mBtLaunchAnnotActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchAnnotActivity();
            }});    }
    private void launchAnnotActivity(){
        Intent intent = new Intent(this, Annotations.class);
        startActivity(intent);}

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
            beginListenForData();
            myLabel.setText("Bluetooth connection Opened");
        }

    }
    void beginListenForData() {
        final Handler handler = new Handler();

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[25]; //[Packet index (1) + Packet delay (4) + Packet size (1) + Max Packet payload (3+16)]
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        //byte[] bytes = IOUtils.toByteArray(mmInputStream);
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            final byte[] packetBytes = new byte[bytesAvailable];
                            int bytesLeft = bytesAvailable;
                            while (bytesLeft > 0) {
                                mmInputStream.read(packetBytes);
                                //byte[] packetIndexBytes = {packetBytes[0]};
                                int packetIndex = Integer.parseInt(String.valueOf(packetBytes[0] & 0x00FF));
                                log.info("packetIndex: " + packetIndex);
                                //byte[] packetDelayBytes = {packetBytes[1], packetBytes[2], packetBytes[3], packetBytes[4]};
                                int packetDelay = Integer.parseInt(String.valueOf((packetBytes[1] & 0x00FF) << 32 | (packetBytes[2] & 0x00FF) << 16 | (packetBytes[3] & 0x00FF) << 8 | packetBytes[4] & 0x00FF));
                                log.info("packetDelay: " + packetDelay);
                                //byte[] packetSizeBytes = {packetBytes[5]};
                                int packetSize = Integer.parseInt(String.valueOf(packetBytes[5] & 0x00FF));
                                log.info("packetSize: " + packetSize);
                                final byte[] packetPayload = new byte[packetSize];
                                System.arraycopy(packetBytes, 6, packetPayload, 0, packetSize);
//                            for (int i = 0; i < bytesAvailable; i++) {
//                                byte b = packetBytes[i];
//
//                                if (b == delimiter) {
//                                    byte[] encodedBytes = new byte[readBufferPosition];
//                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
//                                    final String data = new String(encodedBytes, "US-ASCII");
//                                    readBufferPosition = 0;
//
                                handler.post(new Runnable() {
                                    public void run() {
                                        FileOutputStream outputStream;
                                        FileInputStream inputStream;
                                        try {
                                            log.info("Raw DATA: " + new String(packetPayload));
                                            outputStream = openFileOutput("DataFromASS.txt", Context.MODE_APPEND);
                                            outputStream.write(packetPayload);
                                            outputStream.close();
                                            inputStream = openFileInput("DataFromASS.txt");
                                            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                                            String line = "";
                                            try {
                                                while ((line = br.readLine()) != null)
                                                    log.info("DATA fandroid:color/blackrom file: " + line);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
//
//                                            //myLabel.setText(data);
                                    }
                                });
                                Arrays.copyOfRange(packetBytes, 6 + packetSize, bytesLeft);
                                bytesLeft = bytesLeft - (1 + 4 + 1 + packetSize);
                            }
//                                } else {
//                                    readBuffer[readBufferPosition++] = b;
//                                }
//                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }
    void sendData() throws IOException {
        myLabel.setText("Data Sent");
    }

    void closeBT() throws IOException {
        if (mmSocket != null && mmSocket.isConnected()) {
            stopWorker = true;
            mmInputStream.close();
            mmSocket.close();
            myLabel.setText("Bluetooth Closed");
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

    public void visible(View v) {
        Intent getVisible
                = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }
    public void list(View v) {
        pairedDevices = BA.getBondedDevices();

        ArrayList list = new ArrayList();

        for (BluetoothDevice bt : pairedDevices) list.add(bt.getName());
        Toast.makeText(getApplicationContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }




}

