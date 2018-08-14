package com.av.whereareyou;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecordActivity extends Activity {

    @BindView(R.id.btn_mic)
    ImageButton btnMic;
    @BindView(R.id.btn_stop)
    ImageButton btnStop;
    @BindView(R.id.btn_play)
    ImageButton btnPlay;
    @BindView(R.id.btn_test)
    Button btnTest;

    private MediaRecorder mediaRecorder;

    String filePath;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);

        btnPlay.setVisibility(View.GONE);
        btnTest.setVisibility(View.GONE);

        mediaRecorder = new MediaRecorder();

        filePath = getFilesDir().getAbsolutePath() + "record.3gp";

        File file = new File(filePath);
        if (file.exists()) {
            btnPlay.setVisibility(View.VISIBLE);
            btnTest.setVisibility(View.VISIBLE);
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



    @OnClick(R.id.btn_play)
    public void onPlay(View view) {

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);

        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.d("=====", e.toString());
        }
        mediaPlayer.start();
        btnPlay.setVisibility(View.GONE);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.release();
                btnPlay.setVisibility(View.VISIBLE);
            }
        });
        Toast.makeText(this, "playing", Toast.LENGTH_SHORT).show();
    }


    @OnClick(R.id.btn_mic)
    public void onMic(View view) {

        resetRecorder();

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        btnMic.setVisibility(View.GONE);
        btnStop.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_stop)
    public void onRecordStop(View view) {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        btnStop.setVisibility(View.GONE);
        btnMic.setVisibility(View.VISIBLE);
        btnPlay.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_test)
    public void gotoTestMode(View view) {
        Intent intent = new Intent(RecordActivity.this, TestModeActivity.class);
        startActivity(intent);
    }

    // this process must be done prior to the start of recording
    private void resetRecorder() {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setAudioEncodingBitRate(16);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setOutputFile(filePath);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    @Override
    public void onBackPressed() {

    }
}
