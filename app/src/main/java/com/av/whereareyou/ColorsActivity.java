package com.av.whereareyou;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ColorsActivity extends Activity {

    @BindView(R.id.scrollView)
    ScrollView scrollView;

    @BindView(R.id.txtTitle)
    TextView txtTitle;

    @BindView(R.id.btn_white)
    Button btnWhite;
    @BindView(R.id.btn_continue)
    Button btnContinue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colors);
        ButterKnife.bind(this);

    }


    @OnClick(R.id.btn_blue)
    public void onBlue(View view) {
        this.scrollView.setBackgroundResource(R.color.blue);
        selectWhite(false);
        saveColor("blue");
    }

    @OnClick(R.id.btn_orange)
    public void onOrange(View view) {
        this.scrollView.setBackgroundResource(R.color.orange);
        selectWhite(false);
        saveColor("orange");
    }

    @OnClick(R.id.btn_pink)
    public void onPink(View view) {
        this.scrollView.setBackgroundResource(R.color.pink);
        selectWhite(false);
        saveColor("pink");
    }

    @OnClick(R.id.btn_red)
    public void onRed(View view) {
        this.scrollView.setBackgroundResource(R.color.red);
        selectWhite(false);
        saveColor("red");
    }

    @OnClick(R.id.btn_green)
    public void onGreen(View view) {
        this.scrollView.setBackgroundResource(R.color.green);
        selectWhite(false);
        saveColor("green");
    }

    @OnClick(R.id.btn_white)
    public void onWhite(View view) {
        this.scrollView.setBackgroundResource(R.color.white);
        selectWhite(true);
        saveColor("white");
    }

    @OnClick(R.id.btn_continue)
    public void onContinue(View view) {
        Intent intent = new Intent(ColorsActivity.this, TestColorActivity.class);
        startActivity(intent);
    }

    private void selectWhite(boolean isWhite) {
        if (isWhite) {
            this.btnWhite.setBackgroundResource(R.drawable.black_border_button);
            this.btnContinue.setBackgroundResource(R.drawable.black_border_button);
            btnContinue.setTextColor(getResources().getColor(R.color.black, null));
            txtTitle.setTextColor(getResources().getColor(R.color.black, null));
        } else {
            this.btnWhite.setBackgroundResource(R.drawable.white_round_button);
            this.btnContinue.setBackgroundResource(R.drawable.round_border_button);
            btnContinue.setTextColor(getResources().getColor(R.color.white, null));
            txtTitle.setTextColor(getResources().getColor(R.color.white, null));
        }
    }

    private void saveColor(String color) {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("color", color);
        editor.apply();
    }
}
