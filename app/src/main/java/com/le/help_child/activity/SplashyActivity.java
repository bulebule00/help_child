package com.le.help_child.activity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.le.help_child.MainActivity;
import com.le.help_child.R;

public class SplashyActivity extends AppCompatActivity {
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashy);
        tv = (TextView) findViewById(R.id.timeView);
        MyCountDownTimer mc = new MyCountDownTimer();
        mc.start();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(SplashyActivity.this, MainActivity.class);
                startActivity(intent);
                SplashyActivity.this.finish();
            }
        }, 3000);
    }
    private final Handler handler=new Handler();


    class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer() {
            super((long) 3000, (long) 1000);
        }
        public void onFinish() {
            tv.setText("正在跳转");
        }
        public void onTick(long millisUntilFinished) {
            tv.setText("倒计时(" + millisUntilFinished / 1000 + ")");
        }
    }
}
