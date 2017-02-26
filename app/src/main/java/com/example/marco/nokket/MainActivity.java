package com.example.marco.nokket;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    static final String EXTRA_PAYMENT_AMOUNT = "payment_amount";

    private static final int DONATE_REQUEST = 100;

    private String mPaymentAmount;

    private Button mOneDollarButton;
    private Button mTwoDollarsButton;
    private Button mFiveDollarsButton;
    private Button mTenDollarsButton;
    private Button mTwentyllarsButton;
    private Button mMoreButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOneDollarButton = (Button) findViewById(R.id.one_dollar);
        mTwoDollarsButton = (Button) findViewById(R.id.two_dollars);
        mFiveDollarsButton = (Button) findViewById(R.id.five_dollars);
        mTenDollarsButton = (Button) findViewById(R.id.ten_dollars);
        mTwentyllarsButton = (Button) findViewById(R.id.twenty_dollars);
        mMoreButton = (Button) findViewById(R.id.more);
    }

    public void launchDonate(View v) {

        switch (v.getId()) {
            case R.id.one_dollar:
                mPaymentAmount = "1";
                break;
            case R.id.two_dollars:
                mPaymentAmount = "2";
                break;
            case R.id.five_dollars:
                mPaymentAmount = "5";
                break;
            case R.id.ten_dollars:
                mPaymentAmount = "10";
                break;
            case R.id.twenty_dollars:
                mPaymentAmount = "20";
                break;
        }

        Intent intent = new Intent(this, DonateActivity.class)
                .putExtra(EXTRA_PAYMENT_AMOUNT, mPaymentAmount);
        startActivityForResult(intent, DONATE_REQUEST);
    }
}