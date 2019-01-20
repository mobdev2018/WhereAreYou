package com.av.whereareyou;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class WarningActivity extends Activity {

    private static int BATTERY_SAVER_REQUEST = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.btnYes)
    public void enablePowerSavingMode(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            if (!pm.isPowerSaveMode()) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                startActivityForResult(intent, BATTERY_SAVER_REQUEST);
            }

        }
    }

    @OnClick(R.id.btnNo)
    public void dismissWaning(View view) {
        Intent intent = new Intent(WarningActivity.this, TestColorActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BATTERY_SAVER_REQUEST) {
            Intent intent = new Intent(WarningActivity.this, TestColorActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
