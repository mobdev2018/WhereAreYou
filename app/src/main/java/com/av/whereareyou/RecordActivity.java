package com.av.whereareyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecordActivity extends Activity {

    private static final String TAG = "RecordActivity";

    @BindView(R.id.btn_mic)
    ImageButton btnMic;
    @BindView(R.id.btn_stop)
    ImageButton btnStop;
    @BindView(R.id.btn_test)
    Button btnTest;
    @BindView(R.id.txtVoice)
    TextView txtVoice;

    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    private android.hardware.Camera.Parameters params;

    DroidSpeech droidSpeech;
    Vibrator vib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);

        btnTest.setVisibility(View.GONE);

        hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(RecordActivity.this)
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
        disableBeepSound();
    }

    @OnClick(R.id.btn_mic)
    public void onMic(View view) {

        btnMic.setVisibility(View.GONE);
        btnStop.setVisibility(View.VISIBLE);

        startRecord();
    }

    @OnClick(R.id.btn_stop)
    public void onStop(View view) {
        droidSpeech.closeDroidSpeechOperations();
        btnMic.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_test)
    public void gotoTestMode(View view) {
        droidSpeech.closeDroidSpeechOperations();
        Intent intent = new Intent(RecordActivity.this, TestModeActivity.class);
        startActivity(intent);
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
                Log.d(TAG, "live: " + liveSpeechResult);
                txtVoice.setText("You said <" + liveSpeechResult + ">");
            }

            @Override
            public void onDroidSpeechFinalResult(String finalSpeechResult) {
                Log.d(TAG, finalSpeechResult);
                txtVoice.setText("You said <" + finalSpeechResult + ">");
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
        disableBeepSound();
    }

    private void detected() {

        btnTest.setVisibility(View.VISIBLE);

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(500);


        stopRecord();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (droidSpeech != null) {
            droidSpeech.closeDroidSpeechOperations();
        }
    }

    @Override
    public void onBackPressed() {

    }

    private void stopRecord() {
        btnStop.setVisibility(View.GONE);
        btnMic.setVisibility(View.VISIBLE);
        btnTest.setVisibility(View.VISIBLE);

        turnOnFlash();
    }

    private void disableBeepSound() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        am.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_PLAY_SOUND);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
        am.setStreamVolume(AudioManager.STREAM_ALARM, 0, AudioManager.FLAG_PLAY_SOUND);
        am.setStreamVolume(AudioManager.STREAM_ACCESSIBILITY, 0, AudioManager.FLAG_PLAY_SOUND);
        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, AudioManager.FLAG_PLAY_SOUND);
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
