/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.android.common.logger.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothChatService implements  GameView.IWrite{
    GameView mGameView;
    // Debugging
    private static final String TAG = "BluetoothChatService";

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothChatService(Context context, Handler handler, GameView gameView) {
        android.util.Log.e(TAG+"->"+mState, "BluetoothChatService(Context context, Handler handler)見夠子");
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
        mHandler = handler;

        mGameView=gameView;  mGameView.setCallBack(this);
    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        Log.e(TAG+"->"+mState, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
        mNewState = mState;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.e(TAG+"->"+mState, "BluetoothChatService.start()");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            android.util.Log.e(TAG, "start: mSecureAcceptThread == null");
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            android.util.Log.e(TAG, "start: mInsecureAcceptThread == null" );
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
        // Update UI title
        updateUserInterfaceTitle();  // 0-->1
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.e(TAG+"->"+mState, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        android.util.Log.e(TAG+"->"+mState, "connect(): mConnectThread = new ConnectThread(device, secure) ");
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        // Update UI title
        updateUserInterfaceTitle(); //1-->2
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.e(TAG+"->"+mState, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        android.util.Log.e(TAG+"->"+mState, "connected(): new ConnectedThread( ");
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        // Update UI title
        updateUserInterfaceTitle();  //2-->3
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.e(TAG+"->"+mState, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        mState = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        BluetoothChatService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        BluetoothChatService.this.start();
    }



    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            android.util.Log.e(TAG+"->"+mState, "AcceptThread(boolean secure ): "  );

            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG+"->"+mState, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            android.util.Log.e(TAG+"->"+mState, "AcceptThread: get mmServerSocket=tmp ");
            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        public void run() {
            android.util.Log.e(TAG+"->"+mState, "AcceptThread.run(),run: 跑 ");
            Log.e(TAG+"->"+mState, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;
            android.util.Log.e(TAG+"->"+mState, "前 mState != STATE_CONNECTED ");
            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    android.util.Log.e(TAG, "AcceptThread.run() in  mState != STATE_CONNECTED");
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                    android.util.Log.e(TAG+"->"+mState, "AcceptThread.run(): socket = mmServerSocket.accept() 成功" );
                } catch (IOException e) {
                    Log.e(TAG+"->"+mState, "Socket Type: " + mSocketType + "accept() failed catch 失敗", e);
                    break;
                }
                android.util.Log.e(TAG+"->"+mState, "AcceptThread.run(): get socket 成功" );
                // If a connection was accepted
                if (socket != null) {
                    android.util.Log.e(TAG+"->"+mState, "AcceptThread.run(): socket != null 仍要判斷state" );
                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                android.util.Log.e(TAG+"->"+mState, "AcceptThread.run().connected() STATE_LISTEN,  STATE_CONNECTING " );
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                android.util.Log.e(TAG+"->"+mState, "STATE_NONE or  STATE_CONNECTED" );
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    android.util.Log.e(TAG+"->"+mState, "STATE_NONE or  STATE_CONNECTED 將socket關掉" );
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG+"->"+mState, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.e(TAG+"->"+mState, "END mAcceptThread.run(), socket Type:結束run() " + mSocketType);

        }

        public void cancel() {
            Log.e(TAG+"->"+mState, "Socket Type " + mSocketType + " AcceptThread.cancel() " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG+"->"+mState, "Socket Type " + mSocketType + " close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            android.util.Log.e(TAG+"->"+mState, "ConnectThread(BluetoothDevice device, boolean secure)");
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG+"->"+mState, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run() {
            Log.e(TAG+"->"+mState, " BEGIN mConnectThread SocketType: run()跑 " + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.e(TAG+"->"+mState, "  run(). mmSocket.connect() ");
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    Log.e(TAG+"->"+mState, "  run(). mmSocket.close() ");
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG+"->"+mState, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothChatService.this) {
                Log.e(TAG+"->"+mState, "  run(). mConnectThread = null");
                mConnectThread = null;
            }

            // Start the connected thread
            android.util.Log.e(TAG+"->"+mState, "run().開始ConnectThread.run().connected()");
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            android.util.Log.e(TAG+"->"+mState, " ConnectThread.cancel(): " );
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG+"->"+mState, " close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.e(TAG+"->"+mState, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG+"->"+mState, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run() {
            Log.e(TAG+"->"+mState, " BEGIN mConnectedThread. run() 跑");

            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                DataInputStream dis = null;
                String data = null;
                try {
                    Log.e(TAG+"->"+mState, " mState == STATE_CONNECTED, 開始讀 ");
                    // Read from the InputStream
                    dis = new DataInputStream(mmInStream);
                    data = dis.readUTF();

                    if (data != null) {
                        final String finalData = data;

                        mGameView.post(new Runnable() {
                            @Override
                            public void run() {
                                mGameView.getCommand(finalData);
                            }
                        });
                    }

                    //bytes = mmInStream.read(buffer);
                    //android.util.Log.e(TAG+"->"+mState, "ConnectedThread.run.mmInStream: "+ buffer.toString()  );
                    // Send the obtained bytes to the UI Activity
                    //mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                    //        .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG+"->"+mState, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            //android.util.Log.e(TAG+"->"+mState, "ConnectedThread:write() 開始寫" );
            DataOutputStream dos = null;
            try {
                dos = new DataOutputStream(mmOutStream);
                String temp = new String(buffer, "utf-8");
                dos.writeUTF(temp);

                //mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
               // mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
               //         .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG+"->"+mState, "Exception during write", e);
            }
        }

        public void cancel() {
            android.util.Log.e(TAG+"->"+mState, " ConnectedThread.cancel(): " );
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG+"->"+mState, " close() of connect socket failed", e);
            }
        }
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        if (mState != STATE_CONNECTED) return;

        // Create temporary object
        //android.util.Log.e(TAG+"->"+mState, "BluetoothChatService.write() " );

        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    //發送信息
    @Override
    public void onWrite(String str) {
        write(str.getBytes());
    }

}
