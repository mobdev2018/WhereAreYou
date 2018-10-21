package com.av.whereareyou;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ColorsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colors);
        ButterKnife.bind(this);

    }


    @OnClick(R.id.btn_blue)
    public void onBlue(View view) {

    }

    @OnClick(R.id.btn_orange)
    public void onOrange(View view) {

    }

    @OnClick(R.id.btn_pink)
    public void onPink(View view) {

    }

    @OnClick(R.id.btn_red)
    public void onRed(View view) {

    }

    @OnClick(R.id.btn_green)
    public void onGreen(View view) {

    }

    @OnClick(R.id.btn_white)
    public void onWhite(View view) {

    }

    @OnClick(R.id.btn_continue)
    public void onContinue(View view) {

    }
}
