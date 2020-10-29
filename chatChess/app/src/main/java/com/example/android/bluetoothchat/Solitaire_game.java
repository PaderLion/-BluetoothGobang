package com.example.android.bluetoothchat;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class Solitaire_game extends Activity {

    Button button3;
    Button button5;
    Button buttonswitch;
    public boolean musicswitch=true;
    //---music---
    MediaPlayer mPlayer;
    //---music---
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.solitaire_game);

        //---------------------Music---------------------------------
        music();
//---------------------Music---------------------------------

        button3 = findViewById(R.id.button);//返回首頁
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();

                setResult(2,intent);
                finish();
            }
        });



        button5 = findViewById(R.id.button5);//重玩
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });




        buttonswitch = findViewById(R.id.button7);//音樂開關
        buttonswitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startstopmusic();
            }
        });


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
        buttonswitch.setText("音樂開");
    }
    public void startmusic(){
        mPlayer.start();
        musicswitch=true;
        buttonswitch.setText("音樂關");
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
