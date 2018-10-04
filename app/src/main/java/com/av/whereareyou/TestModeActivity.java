package com.av.whereareyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private String fNmae = "audio.mp3";
    private String fPAth = "android.resource://com.av.whereareyou/raw/audio";

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

        setRingtone();

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(this,uri);

        String ringtoneTitle = ringtone.getTitle(this);
        Toast.makeText(getApplicationContext(), "Ringtone Name : " + ringtoneTitle, Toast.LENGTH_SHORT).show();

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
                if (finalSpeechResult.equals("hello")) {
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


    private void setRingtone() {
        AssetFileDescriptor openAssetFileDescriptor;
        ((AudioManager) getSystemService(AUDIO_SERVICE)).setRingerMode(2);
        File file = new File(Environment.getExternalStorageDirectory() + "/whereareyou", this.fNmae);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Uri parse = Uri.parse(this.fPAth);
        ContentResolver contentResolver = getContentResolver();
        try {
            openAssetFileDescriptor = contentResolver.openAssetFileDescriptor(parse, "r");
        } catch (FileNotFoundException e2) {
            openAssetFileDescriptor = null;
        }
        try {
            byte[] bArr = new byte[1024];
            FileInputStream createInputStream = openAssetFileDescriptor.createInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            for (int read = createInputStream.read(bArr); read != -1; read = createInputStream.read(bArr)) {
                fileOutputStream.write(bArr, 0, read);
            }
            fileOutputStream.close();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("_data", file.getAbsolutePath());
        contentValues.put("title", "nkDroid ringtone");
        contentValues.put("mime_type", "audio/mp3");
        contentValues.put("_size", Long.valueOf(file.length()));
        contentValues.put("artist", Integer.valueOf(R.string.app_name));
        contentValues.put("is_ringtone", Boolean.valueOf(true));
        contentValues.put("is_notification", Boolean.valueOf(false));
        contentValues.put("is_alarm", Boolean.valueOf(false));
        contentValues.put("is_music", Boolean.valueOf(false));
        try {
            Toast.makeText(this, new StringBuilder().append("Ringtone set successfully"), Toast.LENGTH_LONG).show();
            RingtoneManager.setActualDefaultRingtoneUri(getBaseContext(), 1, contentResolver.insert(MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath()), contentValues));
        } catch (Throwable th) {
            Log.d("========", th.toString());
            Toast.makeText(this, new StringBuilder().append("Ringtone feature is not working"), Toast.LENGTH_LONG).show();
        }
    }

}
