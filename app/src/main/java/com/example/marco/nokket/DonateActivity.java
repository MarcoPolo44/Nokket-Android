package com.example.marco.nokket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DonateActivity extends AppCompatActivity {

    private String mPaymentAmount;

    private TextView mAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        mPaymentAmount = getIntent().getStringExtra(MainActivity.EXTRA_PAYMENT_AMOUNT);

        mAmount = (TextView) findViewById(R.id.amount);
        if (mPaymentAmount != null) {
            mAmount.append(mPaymentAmount);
        }
    }
}
