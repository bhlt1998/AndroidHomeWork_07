package com.bytedance.videoplayer;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.SeekBar;

import java.util.Timer;
import java.util.TimerTask;

public class MediaPlayerActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private MediaPlayer player;
    private SurfaceHolder holder;
    private SeekBar skbProgress;
    private boolean isStop = false;
    Handler handleProgress;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("MediaPlayer");
        setContentView(R.layout.activity_media_player);

        surfaceView = findViewById(R.id.surfaceView);
        skbProgress = (SeekBar) findViewById(R.id.seekbar);
        player = new MediaPlayer();
        try{
            player.setDataSource(getResources().openRawResourceFd(R.raw.yuminhong));
            holder = surfaceView.getHolder();
            holder.addCallback(new PlayerCallBack());
            player.prepare();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    player.start();
                    player.setLooping(true);
                }
            });
            player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    System.out.println(percent);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        if(player != null){
            skbProgress.setMax(player.getDuration());
        }
        skbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    player.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        findViewById(R.id.buttonPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.start();
            }
        });
        handleProgress = new Handler(){
            public void handleMessage(Message msg){
                if(isStop == false) {
                    int position = player.getCurrentPosition();
                    int duration = player.getDuration();
                    if (duration > 0) {
                        long pos = position;
                        skbProgress.setProgress((int) pos);
                    }
                }
            }
        };

        new Thread(new SeekBarThread()).start();
        findViewById(R.id.buttonPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        isStop = true;
        if (player != null && player.isPlaying()) {
            player.stop();
            player.release();
        }
    }
    private class PlayerCallBack implements SurfaceHolder.Callback{
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            player.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class SeekBarThread implements Runnable{
        @Override
        public void run() {
            while (player != null && isStop == false){
                handleProgress.sendEmptyMessage(0);
                try{
                    Thread.sleep(100);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
