package com.av.whereareyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestModeActivity extends Activity{

    @BindView(R.id.btn_ok)
    Button btnOK;
    @BindView(R.id.txtVoice)
    TextView txtVoice;

    private static String TAG = "TestModeActivity";

    String testFilePath;

    private Camera camera;
    private boolean isFlashOn = false;
    private boolean hasFlash;
    private android.hardware.Camera.Parameters params;

    DroidSpeech droidSpeech;
    Vibrator vib;
    Ringtone ringtone;

    Timer vibrationTimer;
    TimerTask vibrationTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mode);
        ButterKnife.bind(this);

        btnOK.setVisibility(View.GONE);

        testFilePath = getFilesDir().getAbsolutePath() + "record.wav";

        File file = new File(testFilePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(TestModeActivity.this)
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Sorry, your device doesn't support flash light!");
            alert.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
                }
            });
            alert.show();
        }

        getCamera();

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(this,uri);
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startRecord();
            }
        }, 1000);

    }

    private void startRecord() {
        droidSpeech = new DroidSpeech(this, null);
        droidSpeech.setOnDroidSpeechListener(new OnDSListener() {
            @Override
            public void onDroidSpeechSupportedLanguages(String currentSpeechLanguage, List<String> supportedSpeechLanguages) {
                Log.d(TAG, currentSpeechLanguage);
            }

            @Override
            public void onDroidSpeechRmsChanged(float rmsChangedValue) {
//                Log.d(TAG, "rms");
            }

            @Override
            public void onDroidSpeechLiveResult(String liveSpeechResult) {
                Log.d(TAG, liveSpeechResult);
                txtVoice.setText(liveSpeechResult);
            }

            @Override
            public void onDroidSpeechFinalResult(String finalSpeechResult) {
                Log.d(TAG, finalSpeechResult);
                txtVoice.setText(finalSpeechResult);
                if (finalSpeechResult.equals("where are you")) {
                    droidSpeech.closeDroidSpeechOperations();
                    detected();
                }
            }

            @Override
            public void onDroidSpeechClosedByUser() {
                Log.d(TAG, "onDroidSpeechClosedByUser");
            }

            @Override
            public void onDroidSpeechError(String errorMsg) {
                Log.d(TAG, "error: " + errorMsg);
            }
        });

        droidSpeech.startDroidSpeechRecognition();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        droidSpeech.closeDroidSpeechOperations();
        super.onDestroy();
    }

    @OnClick(R.id.btn_back)
    public void onBack(View view) {
        droidSpeech.closeDroidSpeechOperations();
        if (ringtone.isPlaying()) {
            ringtone.stop();
        }

        if (vibrationTask != null) {
            vibrationTask.cancel();
        }

        turnOffFlash();
        vib.cancel();

        finish();
    }

    @OnClick(R.id.btn_settings)
    public void onSettins(View view) {

    }

    @OnClick(R.id.btn_ok)
    public void onOk(View view) {
        droidSpeech.closeDroidSpeechOperations();
        if (ringtone.isPlaying()) {
            ringtone.stop();
        }

        if (vibrationTimer != null) {
            vibrationTimer.cancel();
        }

        vib.cancel();
        turnOffFlash();

        Intent intent = new Intent(TestModeActivity.this, ConfirmRecordActivity.class);
        startActivity(intent);
    }

    private void detected() {

        btnOK.setVisibility(View.VISIBLE);

        vibrationTimer = new Timer();
        vibrationTask = new TimerTask() {
            @Override
            public void run() {
                turnOnFlash();
                vib.vibrate(1000);
            }
        };

        vibrationTimer.scheduleAtFixedRate(vibrationTask, 0, 1200);

        ringtone.play();

    }

    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e(TAG, "Camera Error. Failed to Open. Error: " + e.getMessage());
            }
        }
    }

    private void turnOnFlash() {
        Log.d(TAG, "======" + String.valueOf(isFlashOn));
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = true;
        }

        turnOffFlash();
    }

    private void turnOffFlash() {
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashOn = false;
        }
    }

}
