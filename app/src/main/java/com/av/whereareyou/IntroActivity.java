package com.av.whereareyou;

import android.app.Activity;
import android.os.Bundle;

import butterknife.ButterKnife;

public class IntroActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);
    }

}