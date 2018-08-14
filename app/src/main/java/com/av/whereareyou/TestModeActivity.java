package com.av.whereareyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestModeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mode);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_back)
    public void onBack(View view) {
        finish();
    }

    @OnClick(R.id.btn_settings)
    public void onSettins(View view) {

    }

    @OnClick(R.id.btn_ok)
    public void onOk(View view) {
        Intent intent = new Intent(TestModeActivity.this, ConfirmRecordActivity.class);
        startActivity(intent);
    }

}
