package com.av.whereareyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestColorActivity extends Activity {

    @BindView(R.id.contentView)
    View contentView;
    @BindView(R.id.btn_ok)
    Button btnOK;
    @BindView(R.id.txtVoice)
    TextView txtVoice;

    @BindView(R.id.btn_back)
    ImageButton btnBack;
    @BindView(R.id.btn_settings)
    ImageButton btnSettings;

    private static String TAG = "TestColorActivity";

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

    private String fNmae = "audio.mp3";
    private String fPAth = "android.resource://com.av.whereareyou/raw/audio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_color);
        ButterKnife.bind(this);

        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        String color = sharedPreferences.getString("color", "blue");
        setBackground(color);

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
            AlertDialog alert = new AlertDialog.Builder(TestColorActivity.this)
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


        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        switch (am.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                Log.i("MyApp","Silent mode");
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                Log.i("MyApp","Vibrate mode");
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                Log.i("MyApp","Normal mode");
                break;
        }

        try {
            am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getValidRingtoneUri(this));

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startRecord();
            }
        }, 1000);

        disableBeepSound();
    }

    private void disableBeepSound() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (am != null) {
            am.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_ALARM, 0, AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    private void enableBeepSound() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (am != null) {
            am.setStreamVolume(AudioManager.STREAM_SYSTEM, 1, AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 1, AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_ALARM, 1, AudioManager.FLAG_PLAY_SOUND);
            am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 1, AudioManager.FLAG_PLAY_SOUND);
        }
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
                    enableBeepSound();
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
    public void onPause() {
        super.onPause();

        if (ringtone.isPlaying()) {
            ringtone.stop();
        }

        if (vibrationTimer != null) {
            vibrationTimer.cancel();
        }

        vib.cancel();
        turnOffFlash();
    }

    @Override
    public void onDestroy() {
        droidSpeech.closeDroidSpeechOperations();

        if (ringtone.isPlaying()) {
            ringtone.stop();
        }

        if (vibrationTimer != null) {
            vibrationTimer.cancel();
        }

        vib.cancel();
        turnOffFlash();

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

        if (vibrationTask != null) {
            vibrationTask.cancel();
        }

        if (vibrationTimer != null) {
            vibrationTimer.cancel();
        }

        vib.cancel();
        turnOffFlash();

        Intent intent = new Intent(TestColorActivity.this, ConfirmRecordActivity.class);
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


    private void setBackground(String colorString) {
        if (colorString.equals("blue")) {
            contentView.setBackgroundResource(R.color.blue);
        } else if (colorString.equals("orange")) {
            contentView.setBackgroundResource(R.color.orange);
        } else if (colorString.equals("pink")) {
            contentView.setBackgroundResource(R.color.pink);
        } else if (colorString.equals("red")) {
            contentView.setBackgroundResource(R.color.red);
        } else if (colorString.equals("green")) {
            contentView.setBackgroundResource(R.color.green);
        } else if (colorString.equals("white")) {
            contentView.setBackgroundResource(R.color.white);
            btnBack.setImageDrawable(getDrawable(R.drawable.back_black));
            btnSettings.setImageDrawable(getDrawable(R.drawable.settings_black));
            btnOK.setBackgroundResource(R.drawable.black_border_button);
            btnOK.setTextColor(getResources().getColor(R.color.black, null));
            txtVoice.setTextColor(getResources().getColor(R.color.black, null));
        }
    }


}
