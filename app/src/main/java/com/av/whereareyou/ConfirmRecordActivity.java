package com.av.whereareyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConfirmRecordActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_record);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_continue)
    public void onContinue(View view) {
        Intent intent = new Intent(ConfirmRecordActivity.this, RingtonesActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_record_again)
    public void onRecordAgain(View view) {
        Intent intent = new Intent(ConfirmRecordActivity.this, RecordActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        finish();
    }

    @Override
    public void onBackPressed() {

    }
}
