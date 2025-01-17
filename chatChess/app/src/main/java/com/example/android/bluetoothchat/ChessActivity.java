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

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.common.logger.Log;

//import android.app.Fragment;
//import android.support.v4.app.FragmentActivity;
/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class ChessActivity extends Activity {
    GameView mGameView;
    StartGame startGame;
    Button button4;
    private static final String TAG = "ChessActivity";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    public TextView mtextView;
    public Button btnres;
    public Button btnrechess;
    public int sum=0;

    MediaPlayer mPlayer;


    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    public boolean musicswitch=true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess);
        initView();


        music();


        button4 = findViewById(R.id.button4);//音樂開關
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startstopmusic();
            }
        });




        btnres.setOnClickListener(new Button.OnClickListener() { //重玩
            @Override
            public void onClick(View v) {
                restartgame();
            }
        });

        btnrechess.setOnClickListener(new Button.OnClickListener() { //悔棋
            @Override
            public void onClick(View v) {
                returnchess();
            }
        });


       // setHasOptionsMenu(true);
        android.util.Log.e(TAG, "onCreate: ");
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            android.util.Log.e(TAG, "onCreate: "+"mBluetoothAdapter = null");
            //FragmentActivity activity = getActivity();
            //Activity activity = getActivity();
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            //activity.finish();
        }
        android.util.Log.e(TAG, "onCreate: "+"mBluetoothAdapter != null");
    }

    public void startstopmusic(){
        if(musicswitch){
            stopmusic();
        }
        else {
            startmusic();
        }
    }
    public void stopmusic(){
        mPlayer.pause();
        musicswitch=false;
        button4.setText("音樂開");
    }
    public void startmusic(){
        mPlayer.start();
        musicswitch=true;
        button4.setText("音樂關");
    }


    public void initView() {
        android.util.Log.e(TAG, "onViewCreated: ");
        mGameView=(GameView) findViewById(R.id.gameview1);
//        mConversationView = (ListView) findViewById(R.id.in);
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mSendButton = (Button) findViewById(R.id.button_send);
        mtextView= (TextView) findViewById(R.id.edit_text_out);
        btnres=findViewById(R.id.btn_pgv_again);
        btnrechess=findViewById(R.id.btn_pgv_return);


    }

    @Override
    public void onStart() {
        super.onStart();
        android.util.Log.e(TAG, "onStart: ");
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        android.util.Log.e(TAG, "onStart: 1" );
        if (mBluetoothAdapter != null) {
            android.util.Log.e(TAG, "onStart: 2" );
            if (!mBluetoothAdapter.isEnabled()) {
                android.util.Log.e(TAG, "onStart: 3" + "!mBluetoothAdapter.isEnabled()");
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the chat session
            }else if (mChatService == null) {
                android.util.Log.e(TAG, "onStart: 4: setupChat()" );
                setupChat();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        android.util.Log.e(TAG, "onDestroy: ");
        if (mChatService != null) {
            mChatService.stop();
        }

        //music add
        mPlayer.release();

    }

    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.e(TAG, "onResume: ");
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                android.util.Log.e(TAG, "onResume: mChatService.start()");
                mChatService.start();
            }
        }
//music add
        mPlayer.start();
    }

    public void restartgame(){
        btnres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message="restart";
                sendMessage(message);
                mGameView.restartGame();
            }
        });
    }

    public void returnchess(){
        btnrechess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mGameView.Myturn){
                    if(sum<1){
                        if(mGameView.returnchess){
                            String message="return";
                            sendMessage(message);
                            mGameView.returnUp();
                            sum++;
                        }

                    }
                    else{
                        Toast.makeText(ChessActivity.this, "只能悔一次棋",
                                Toast.LENGTH_SHORT).show();
                      //  Toast.makeText(ChessActivity.this, "只能悔一次棋(⊙o⊙)？死傻逼",Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }


    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.e(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
//        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
//        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                android.util.Log.e(TAG, "mSendButton.onClick: ");
                    String message ="msg;" + mtextView.getText().toString();
                    sendMessage(message);
            }
        });

        android.util.Log.e(TAG, "setupChat: new BluetoothChatService()");
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler, mGameView);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
       // Activity activity = getActivity(); //FragmentActivity activity = getActivity();
       // if (null == activity) {
       //     return;
       // }
        final ActionBar actionBar = this.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
       // Activity activity = getActivity();//FragmentActivity activity = getActivity();
       // if (null == activity) {
       //     return;
      //  }
        final ActionBar actionBar = this.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
           // Activity activity = getActivity(); //FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                           // mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                 //   mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
               //     mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);

                        Toast.makeText(ChessActivity.this, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    break;
                case Constants.MESSAGE_TOAST:

                        Toast.makeText(ChessActivity.this, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();

                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.e(TAG, "BT not enabled");

                    Intent intent = new Intent();

                    setResult(2,intent);
                    finish();


                    Toast.makeText(ChessActivity.this, "請開啟藍芽",
                    Toast.LENGTH_SHORT).show();

                    Toast.makeText(ChessActivity.this, "藍牙未連線",
                            Toast.LENGTH_SHORT).show();

                    //getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        android.util.Log.e(TAG, "connectDevice().mChatService.connect(device, secure) " );
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bluetooth_chat, menu);
        return true;
       // return super.onCreateOptionsMenu(menu);
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.bluetooth_chat, menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        android.util.Log.e(TAG, "onOptionsItemSelected(): "  );
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(ChessActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(ChessActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }






    //---------------------Music---------------------------------
    void music()

    //http://jamiedeveloper.pixnet.net/blog/post/247345717-%3Candroid-studio
    // %3E-%E4%BD%BF%E7%94%A8media-player
    // %E5%8A%A0%E5%85%A5%26%E6%8E%A7%E5%88%B6%E9%81%8A%E6%88%B2%E8%83%8C%E6%99%AF

    {
        try
        {
            mPlayer = MediaPlayer.create(this, R.raw.blue);//音樂的檔名(title)在raw裡面
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mPlayer.setLooping(true);

            //重複播放
            //mPlayer.prepare();
            //特別使用批註的方式, 是為了提醒大家, 由於我們先前使用create method建立MediaPlayer
            //create method會自動的call prepare(),
            // 所以我們再call prepare() method會發生 prepareAsync called in state 8的錯誤

        }catch (IllegalStateException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

//---------------------Music---------------------------------


    //---------------------Music---------------------------------

    protected void onPause()
    {
        // TODO Auto-generated method stub
        super.onPause();
        mPlayer.pause();
    }

//---------------------Music---------------------------------









}
