package com.av.whereareyou;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestColorActivity extends Activity {

    @BindView(R.id.contentView)
    View contentView;
    @BindView(R.id.btn_back)
    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_color);
        ButterKnife.bind(this);


        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        String color = sharedPreferences.getString("color", "blue");
        setBackground(color);
    }

    private void setBackground(String colorString) {
        if (colorString.equals("blue")) {
            contentView.setBackgroundResource(R.color.blue);
        } else if (colorString.equals("orange")) {
            contentView.setBackgroundResource(R.color.orange);
        } else if (colorString.equals("pink")) {
            contentView.setBackgroundResource(R.color.pink);
        } else if (colorString.equals("red")) {
            contentView.setBackgroundResource(R.color.red);
        } else if (colorString.equals("green")) {
            contentView.setBackgroundResource(R.color.green);
        } else if (colorString.equals("white")) {
            contentView.setBackgroundResource(R.color.white);
            btnBack.setBackgroundResource(R.drawable.black_border_button);
            btnBack.setTextColor(getResources().getColor(R.color.black, null));
        }
    }

    @OnClick(R.id.btn_back)
    public void onBack(View view) {
        finish();
    }
}
