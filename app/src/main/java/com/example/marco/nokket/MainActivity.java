package com.example.marco.nokket;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Marco on 3/11/2017.
 */

public class MainActivity extends AppCompatActivity {

    /**
     * Intent extras
     */
    static final String EXTRA_PAYMENT_AMOUNT = "payment_amount";

    /**
     * Payment amount
     */
    private String mPaymentAmount;

    /**
     * Amount buttons
     */
    private Button mOneDollarButton;
    private Button mTwoDollarsButton;
    private Button mFiveDollarsButton;
    private Button mTenDollarsButton;
    private Button mTwentyDollarsButton;
    private Button mFiftyDollarsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get views
        mOneDollarButton = (Button) findViewById(R.id.one_dollar);
        mTwoDollarsButton = (Button) findViewById(R.id.two_dollars);
        mFiveDollarsButton = (Button) findViewById(R.id.five_dollars);
        mTenDollarsButton = (Button) findViewById(R.id.ten_dollars);
        mTwentyDollarsButton = (Button) findViewById(R.id.twenty_dollars);
        mFiftyDollarsButton = (Button) findViewById(R.id.fifty_dollars);
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
            case R.id.fifty_dollars:
                mPaymentAmount = "50";
                break;
        }

        Intent intent = new Intent(this, DonateActivity.class)
                .putExtra(EXTRA_PAYMENT_AMOUNT, mPaymentAmount);
        startActivity(intent);
    }
}
