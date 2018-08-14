package com.av.whereareyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class IntroActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_continue)
    public void onContinue(View view) {
        Intent intent = new Intent(IntroActivity.this, RecordActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

    }

}
