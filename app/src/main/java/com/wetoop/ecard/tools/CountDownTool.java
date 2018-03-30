package com.wetoop.ecard.tools;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

/**
 * @author Parck.
 * @date 2017/10/16.
 * @desc 倒计时工具类
 */
public class CountDownTool {

    private boolean running;
    private int timeTotal = 60;
    private int countDownTime = 60;
    private TextView countDownButton;
    private OnCountDownListener onCountDownListener;

    public CountDownTool(TextView countDownButton) {
        init(countDownButton, null);
    }

    public CountDownTool(int timeTotal, TextView countDownButton) {
        this.timeTotal = timeTotal;
        this.countDownTime = timeTotal;
        init(countDownButton, null);
    }

    public CountDownTool(TextView countDownButton, OnCountDownListener onCountDownListener) {
        init(countDownButton, onCountDownListener);
    }

    private void init(TextView countDownButton, OnCountDownListener onCountDownListener) {
        this.countDownButton = countDownButton;
        this.onCountDownListener = onCountDownListener;
        countDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            post(countDownTask);
        }
    };

    private Runnable countDownTask = new Runnable() {
        @Override
        public void run() {
            if (countDownTime > 0) {
                countDownTime--;
                countDownButton.setText(countDownTime + "s");
                handler.postDelayed(this, 1000 * 1);
            } else {
                stop();
            }
        }
    };

    public void start() {
        running = true;
        if (onCountDownListener != null) running = onCountDownListener.onStart();
        if (running) {
            countDownButton.setEnabled(false);
            handler.post(countDownTask);
        }
    }

    public void stop() {
        running = false;
        if (onCountDownListener != null) onCountDownListener.onStop();
        handler.removeCallbacks(countDownTask);
        countDownButton.setEnabled(true);
        countDownButton.setText("重新获取");
        countDownTime = timeTotal;
    }

    public void setOnCountDownListener(OnCountDownListener onCountDownListener) {
        this.onCountDownListener = onCountDownListener;
    }

    public void finish() {
        if (running) stop();
        handler = null;
        countDownTask = null;
        countDownButton = null;
    }

    //=====================================================================
    // inner class
    //=====================================================================

    public abstract static class OnCountDownListener {
        public abstract boolean onStart();

        public void onStop() {
        }
    }

}
