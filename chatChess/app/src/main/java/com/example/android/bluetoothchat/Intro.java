package com.example.android.bluetoothchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class Intro extends Activity {

    Button buttoninfo;
    Button button001;
    Button button002;
    Button button003;
    Button button004;
    MediaPlayer mPlayer;
    public boolean musicswitch=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.intro_layout);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // setContentView(R.layout.intro_alertdialog_001);
        music();

        buttoninfo = findViewById(R.id.button6);//跳首頁
        buttoninfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();

                setResult(2,intent);
                finish();
            }
        });




        button001 = findViewById(R.id.button001);
        button001.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(Intro.this);
                final View v = inflater.inflate(R.layout.intro_alertdialog_001, null);
                new AlertDialog.Builder(Intro.this)
                        .setTitle("關於五子棋")
                        .setView(v)
                        // .setMessage("02")
                        .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                //  finish();
                                closeContextMenu();
                            }
                        }).show();


            }
        });

//---------------------------------------------------------------------------------


        button002 = findViewById(R.id.button002);
        button002.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(Intro.this);
                final View v = inflater.inflate(R.layout.intro_alertdialog_002, null);
                new AlertDialog.Builder(Intro.this)
                        .setTitle("關於藍芽")

                        // .setMessage("02")
                        .setView(v)
                        .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                //  finish();
                                closeContextMenu();
                            }
                        }).show();


            }
        });

//---------------------------------------------------------------------------------


        button003 = findViewById(R.id.button003);
        button003.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(Intro.this)
                        .setTitle("關於我們")

                        .setMessage("指導老師 : 張世旭 \n" +
                                "組員 : 葉志傑(F0406044)\n" +
                                "           郭翰達(F0406061)\n" +
                                "           馬安慶(F0406036)\n" +
                                "           林家陞(F0406601)"
                        )

                        .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                //  finish();
                                closeContextMenu();
                            }
                        }).show();


            }
        });


/*
        button004 = findViewById(R.id.button11);//音樂開關
        button004.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startstopmusic();
            }
        });
*/

    }




    //---------------------Music---------------------------------
    void music()

    //http://jamiedeveloper.pixnet.net/blog/post/247345717-%3Candroid-studio
    // %3E-%E4%BD%BF%E7%94%A8media-player
    // %E5%8A%A0%E5%85%A5%26%E6%8E%A7%E5%88%B6%E9%81%8A%E6%88%B2%E8%83%8C%E6%99%AF

    {
        try
        {
            mPlayer = MediaPlayer.create(this, R.raw.away);//音樂的檔名(title)在raw裡面
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
    @Override
    protected void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        mPlayer.start();
    }
    @Override
    protected void onPause()
    {
        // TODO Auto-generated method stub
        super.onPause();
        mPlayer.pause();
    }
    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
        mPlayer.release();
    }
//---------------------Music---------------------------------

}
