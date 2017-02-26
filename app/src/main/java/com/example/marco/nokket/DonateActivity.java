package com.example.marco.nokket;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marco.nokket.provider.Provider;
import com.example.marco.nokket.utils.NFCUtils;
import com.example.marco.nokket.utils.SimpleAsyncTask;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvParser;
import com.github.devnied.emvnfccard.utils.AtrUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collection;

import fr.devnied.bitlib.BytesUtils;

public class DonateActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        mPaymentAmount = getIntent().getStringExtra(MainActivity.EXTRA_PAYMENT_AMOUNT);

        mAmount = (TextView) findViewById(R.id.amount);
        if (mPaymentAmount != null) {
            mAmount.append(mPaymentAmount);
        }

        // init NfcUtils
        mNfcUtils = new NFCUtils(this);

        // Read card on launch
        if (getIntent().getAction() == NfcAdapter.ACTION_TECH_DISCOVERED) {
            onNewIntent(getIntent());
        }
    }

    @Override
    protected void onResume() {
        mNfcUtils.enableDispatch();

        // Check if NFC is available
        if (!NFCUtils.isNfcAvailable(getApplicationContext())) {
            // NFC not available
            Toast.makeText(this, "NFC not available", Toast.LENGTH_LONG).show();
        }

        // Check if NFC is enabled
        if (!NFCUtils.isNfcEnabled(getApplicationContext())) {
            // NFC not enabled
            Toast.makeText(this, "NFC not enabled", Toast.LENGTH_LONG).show();
        }
        super.onResume();
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
                                Toast.makeText(DonateActivity.this,
                                        "Card Number: " + mCard.getCardNumber() + "\nExpiry: " + mCard.getExpireDate(),
                                        Toast.LENGTH_LONG).show();
                                mReadCard = mCard;
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
