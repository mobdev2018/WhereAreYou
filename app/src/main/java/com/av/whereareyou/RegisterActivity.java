package com.av.whereareyou;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_signup)
    public void onSignUp(View view) {

    }

    @OnClick(R.id.btn_signin)
    public void onSignIn(View view) {
        finish();
    }
}
