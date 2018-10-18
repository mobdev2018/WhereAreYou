package com.av.whereareyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FlashPatternsActivity extends Activity {

    private static String TAG = "FlashPatternsActivity";
    private boolean isFlashOn = false;
    private boolean hasFlash;

    private Camera camera;
    private android.hardware.Camera.Parameters params;

    Timer flashTimer;
    TimerTask flashTask;

    int interval = 0;
    int step = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_patterns);
        ButterKnife.bind(this);

        hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(FlashPatternsActivity.this)
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

        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        interval = sharedPreferences.getInt("flashInterval", 0);

        if (interval == 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("flashInterval", 5);
            editor.apply();
            interval = 5;
        }


        flashTimer = new Timer();
        flashTask = new TimerTask() {
            @Override
            public void run() {
                flash();
            }
        };

        flashTimer.scheduleAtFixedRate(flashTask, 0, 200);
    }

    private void flash() {
        step += 1;
        if (step >= interval) {
            turnOnFlash();
            step = 0;
        }
    }

    @OnClick(R.id.btn_fast)
    public void onFast(View view) {
        if (interval > 2) {
            interval -= 1;

            SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("flashInterval", interval);
            editor.apply();
        }

    }

    @OnClick(R.id.btn_slow)
    public void onSlow(View view) {
        interval += 1;
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("flashInterval", interval);
        editor.apply();
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

    @Override
    public void onDestroy() {

        if (flashTimer != null) {
            flashTimer.cancel();
        }

        turnOffFlash();

        super.onDestroy();
    }
}
