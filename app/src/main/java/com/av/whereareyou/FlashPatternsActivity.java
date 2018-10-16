package com.av.whereareyou;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FlashPatternsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_patterns);
        ButterKnife.bind(this);


    }

    @OnClick(R.id.btn_fast)
    public void onFast(View view) {

    }

    @OnClick(R.id.btn_slow)
    public void onSlow(View view) {

    }
}
