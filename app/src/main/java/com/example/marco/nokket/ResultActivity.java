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

    /**
     * Payment success/failure
     */
    public boolean mPaymentResult;

    /**
     * Result status
     */
    private TextView mResultStatus;

    /**
     * Result image
     */
    private ImageView mResultImage;

    /**
     * Result text
     */
    private TextView mResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Get intents
        mPaymentResult = getIntent().getBooleanExtra(DonateActivity.EXTRA_PAYMENT_RESULT, false);

        // Get views
        mResultStatus = (TextView) findViewById(R.id.result_status);
        mResultImage = (ImageView) findViewById(R.id.result_image);
        mResultText = (TextView) findViewById(R.id.result_text);

        // Check payment result
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
}
