package com.av.whereareyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class RingtonesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtones);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_default)
    public void onDefault(View view) {

    }

    @OnClick(R.id.btn_available)
    public void onRingtoneAvailable(View view) {

    }

    @OnClick(R.id.btn_continue)
    public void onContinue(View view) {
        Intent intent = new Intent(RingtonesActivity.this, FlashPatternsActivity.class);
        startActivity(intent);
    }
}
