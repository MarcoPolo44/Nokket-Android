package com.example.marco.nokket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Marco on 3/11/2017.
 */

public class ResultActivity extends AppCompatActivity {

    public boolean mPaymentResult;
    private TextView mResultStatus;
    private ImageView mResultImage;
    private TextView mResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        mResultStatus = (TextView) findViewById(R.id.result_status);
        mResultImage = (ImageView) findViewById(R.id.result_image);
        mResultText = (TextView) findViewById(R.id.result_text);

        mPaymentResult = getIntent().getBooleanExtra(DonateActivity.EXTRA_PAYMENT_RESULT, false);

        if (mPaymentResult) {
            mResultStatus.setText(R.string.success);
            mResultImage.setImageResource(R.drawable.thumbs_up);
            mResultText.setText(R.string.success_text);
        }
        else {
            mResultStatus.setText(R.string.error);
            mResultImage.setImageResource(R.drawable.negative_smiley);
            mResultText.setText(R.string.error_text);
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(ResultActivity.this, MainActivity.class));
        finish();
    }
}
