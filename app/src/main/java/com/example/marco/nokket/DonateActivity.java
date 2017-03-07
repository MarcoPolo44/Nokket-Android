package com.example.marco.nokket;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.Card;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.example.marco.nokket.provider.Provider;
import com.example.marco.nokket.utils.NFCUtils;
import com.example.marco.nokket.utils.SimpleAsyncTask;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvParser;
import com.github.devnied.emvnfccard.utils.AtrUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.IOException;
import java.util.Collection;

import fr.devnied.bitlib.BytesUtils;
import cz.msebera.android.httpclient.Header;

public class DonateActivity extends AppCompatActivity implements PaymentMethodNonceCreatedListener,
        BraintreeCancelListener, BraintreeErrorListener {

    /**
     * Payment amount
     */
    private String mPaymentAmount;
    private TextView mAmount;

    /**
     * Nfc utils
     */
    private NFCUtils mNfcUtils;

    /**
     * IsoDep provider
     */
    private Provider mProvider = new Provider();

    /**
     * Emv card
     */
    private EmvCard mReadCard;

    /**
     * Last Ats
     */
    private byte[] lastAts;

    /**
     * Braintree
     */
    private String mAuthorization;
    private BraintreeFragment mBraintreeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        mPaymentAmount = getIntent().getStringExtra(MainActivity.EXTRA_PAYMENT_AMOUNT);

        mAmount = (TextView) findViewById(R.id.amount);
        if (mPaymentAmount != null) {
            mAmount.append(mPaymentAmount);
        }

        // Initialise NfcUtils
        mNfcUtils = new NFCUtils(this);

        // Get Braintree client token
        fetchAuthorization();
    }

    protected void fetchAuthorization() {
        mAuthorization = null;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://www.nokket.com/api/client_token", new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                Toast.makeText(DonateActivity.this, "Client Token: Success", Toast.LENGTH_LONG).show();

                try {
                    mAuthorization = response.getString("client_token");
                    onAuthorizationFetched();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                Toast.makeText(DonateActivity.this, "Client Token: Failure", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onAuthorizationFetched() {
        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this, mAuthorization);
            enableNfcPayment(true);
        } catch (InvalidArgumentException e) {
            Toast.makeText(DonateActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        // Check Braintree authorisation
        if (mAuthorization != null)
        {
            enableNfcPayment(true);
        }
        super.onResume();
    }

    private void enableNfcPayment(boolean enable) {
        mNfcUtils.enableDispatch();

        if (!NFCUtils.isNfcAvailable(getApplicationContext())) {
            // NFC not available
            Toast.makeText(this, "NFC not available", Toast.LENGTH_LONG).show();
        }

        if (!NFCUtils.isNfcEnabled(getApplicationContext())) {
            // NFC not enabled
            Toast.makeText(this, "NFC not enabled", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcUtils.disableDispatch();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        final Tag mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (mTag != null) {

            new SimpleAsyncTask() {

                /**
                 * Tag comm
                 */
                private IsoDep mTagcomm;

                /**
                 * Emv Card
                 */
                private EmvCard mCard;

                /**
                 * Boolean to indicate exception
                 */
                private boolean mException;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    mProvider.getLog().setLength(0);
                    Toast.makeText(DonateActivity.this, "Reading card", Toast.LENGTH_LONG).show();
                }

                @Override
                protected void doInBackground() {

                    mTagcomm = IsoDep.get(mTag);
                    if (mTagcomm == null) {
                        Toast.makeText(DonateActivity.this, "Read error", Toast.LENGTH_LONG).show();
                        return;
                    }
                    mException = false;

                    try {
                        mReadCard = null;
                        // Open connection
                        mTagcomm.connect();
                        lastAts = getAts(mTagcomm);

                        mProvider.setmTagCom(mTagcomm);

                        EmvParser parser = new EmvParser(mProvider, true);
                        mCard = parser.readEmvCard();
                        if (mCard != null) {
                            mCard.setAtrDescription(extractAtsDescription(lastAts));
                        }

                    } catch (IOException e) {
                        mException = true;
                    } finally {
                        // close tagcomm
                        IOUtils.closeQuietly(mTagcomm);
                    }
                }

                @Override
                protected void onPostExecute(final Object result) {

                    if (!mException) {
                        if (mCard != null) {
                            if (StringUtils.isNotBlank(mCard.getCardNumber())) {
                                mReadCard = mCard;
                                makePayment();
                            } else if (mCard.isNfcLocked()) {
                                Toast.makeText(DonateActivity.this, "NFC locked", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(DonateActivity.this, "Unknown card error", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(DonateActivity.this, "NFC communication error", Toast.LENGTH_LONG).show();
                    }
                }

            }.execute();
        }

    }

    private void makePayment()
    {
        String cardNumber;
        String cardMonth;
        String cardYear;

        cardNumber = String.valueOf(mReadCard.getCardNumber());
        cardMonth = (String) DateFormat.format("MM",   mReadCard.getExpireDate());
        cardYear = (String) DateFormat.format("yyyy", mReadCard.getExpireDate());

        Toast.makeText(DonateActivity.this,
                "Card Number: " + cardNumber + "\nExpiry: " + cardMonth + "/" + cardYear,
                Toast.LENGTH_LONG).show();

        // Hack for Braintree sandbox
        cardNumber = "4111111111111111";

        // Create the Braintree payment nonce
        CardBuilder cardBuilder = new CardBuilder()
                .cardNumber(cardNumber)
                .expirationMonth(cardMonth)
                .expirationYear(cardYear);

        Card.tokenize(mBraintreeFragment, cardBuilder);
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        Toast.makeText(DonateActivity.this, "Payment Method Nonce Created", Toast.LENGTH_LONG).show();
        String nonce = paymentMethodNonce.getNonce();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("nonce", nonce);
        params.put("amount", mPaymentAmount);
        client.post("http://www.nokket.com/api/nonce/transaction", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    String message = response.getString("message");

                    if (message != null && message.startsWith("created")) {

                        Toast.makeText(DonateActivity.this,
                                "Payment Success: " + message, Toast.LENGTH_LONG).show();
                    } else {
                        if (TextUtils.isEmpty(message)) {

                            Toast.makeText(DonateActivity.this,
                                    "Payment Failure: Server response was empty or malformed", Toast.LENGTH_LONG).show();
                        } else {

                            Toast.makeText(DonateActivity.this,
                                    "Payment Failure: " + message, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                Toast.makeText(DonateActivity.this, "Payment Failure", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCancel(int requestCode) {
        Toast.makeText(DonateActivity.this, "Payment Cancelled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(Exception error) {
        Toast.makeText(DonateActivity.this, "Payment Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
    }

    /**
     * Get ATS from isoDep
     *
     * @param pIso
     *            isodep
     * @return ATS byte array
     */
    private byte[] getAts(final IsoDep pIso) {
        byte[] ret = null;
        if (pIso.isConnected()) {
            // Extract ATS from NFC-A
            ret = pIso.getHistoricalBytes();
            if (ret == null) {
                // Extract ATS from NFC-B
                ret = pIso.getHiLayerResponse();
            }
        }
        return ret;
    }

    /**
     * Method used to get description from ATS
     *
     * @param pAts
     *            ATS byte
     */
    public Collection<String> extractAtsDescription(final byte[] pAts) {
        return AtrUtils.getDescriptionFromAts(BytesUtils.bytesToString(pAts));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
