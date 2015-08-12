package io.payworks.ares.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.EnumSet;

import io.mpos.accessories.AccessoryFamily;
import io.mpos.provider.ProviderMode;
import io.mpos.transactions.Currency;
import io.mpos.ui.ares.R;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.model.MposUiConfiguration;

public class AresDeepLinkActivity extends Activity {

    public static final String TAG = "AresDeepLinkActivity";

    private static final String PARAM_MERCHANT_IDENTIFIER = "merchantIdentifier";
    private static final String PARAM_MERCHANT_SECRET_KEY = "merchantSecretKey";
    private static final String PARAM_PROVIDER_MODE = "providerMode";
    private static final String PARAM_ACCESSORY_FAMILY = "accessoryFamily";

    private static final String PARAM_SESSION_IDENTIFIER = "sessionIdentifier";

    private static final String PARAM_AMOUNT = "amount";
    private static final String PARAM_CURRENCY = "currency";
    private static final String PARAM_SUBJECT = "subject";
    private static final String PARAM_CUSTOM_IDENTIFIER = "customIdentifier";

    private MposUi mMposUi;

    private String mMerchantIdentifier;
    private String mMerchantSecretKey;
    private ProviderMode mProviderMode;
    private AccessoryFamily mAccessoryFamily;

    private String mSessionIdentifier;

    private BigDecimal mAmount;
    private Currency mCurrency;
    private String mSubject;
    private String mCustomIdentifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ares);

        Uri deepLinkUri = getIntent().getData(); //This is the data we get from the browser.
        Log.d(TAG, deepLinkUri.toString());

        if (deepLinkUri != null) {

            if (deepLinkUri.getQueryParameter(PARAM_MERCHANT_IDENTIFIER) != null
                    && deepLinkUri.getQueryParameter(PARAM_MERCHANT_SECRET_KEY) != null
                    && deepLinkUri.getQueryParameter(PARAM_PROVIDER_MODE) != null
                    && deepLinkUri.getQueryParameter(PARAM_ACCESSORY_FAMILY) != null) {

                mMerchantIdentifier = deepLinkUri.getQueryParameter(PARAM_MERCHANT_IDENTIFIER);
                mMerchantSecretKey = deepLinkUri.getQueryParameter(PARAM_MERCHANT_SECRET_KEY);
                mProviderMode = ProviderMode.valueOf(deepLinkUri.getQueryParameter(PARAM_PROVIDER_MODE)); // Make sure they're named the same as in the ProviderMode enum
                mAccessoryFamily = AccessoryFamily.valueOf(deepLinkUri.getQueryParameter(PARAM_ACCESSORY_FAMILY)); // Make sure they're named the same as in the AccessoryFamily enum

                Log.d(TAG, "merchantId:" + mMerchantIdentifier + " merchantSecret:" + mMerchantSecretKey + " providerMode:" + mProviderMode.name() + " accessoryFamily:" + mAccessoryFamily.name());

                // Check for session identifier and continue the transaction.
                if (deepLinkUri.getQueryParameter(PARAM_SESSION_IDENTIFIER) != null) {

                    mSessionIdentifier = deepLinkUri.getQueryParameter(PARAM_SESSION_IDENTIFIER);
                    Log.d(TAG, "sessionId:" + mSessionIdentifier);
                    chargeWithSessionIdentifier();

                } else { // Now look for amount and other required parameters to start transaction. We're not using this for now. But it's nice to have nonetheless.

                    mAmount = new BigDecimal(deepLinkUri.getQueryParameter(PARAM_AMOUNT));
                    mCurrency = Currency.valueOf(deepLinkUri.getQueryParameter(PARAM_CURRENCY));
                    mSubject = deepLinkUri.getQueryParameter(PARAM_SUBJECT);
                    mCustomIdentifier = deepLinkUri.getQueryParameter(PARAM_CUSTOM_IDENTIFIER);
                    Log.d(TAG, "amount:" + mAmount + " currency:" + mCurrency + " subject:" + mSubject + " customIdentifier:" + mCustomIdentifier);
                    chargeWithAmount();
                }
            } else {
                showError();
            }
        } else {
            showError();
        }
        finish();
    }

    /**
     *Charging with a Session identifier.
     */
    private void chargeWithSessionIdentifier() {
        initializeMposUi();
        Intent intent = mMposUi.createTransactionIntent(mSessionIdentifier);
        startActivityForResult(intent, MposUi.REQUEST_CODE_PAYMENT);
    }

    /**
     * Charging with Amount-Currency-Subject-CustomIdentifier
     */
    private void chargeWithAmount() {
        initializeMposUi();
        Intent intent = mMposUi.createChargeTransactionIntent(mAmount, mCurrency, mSubject, mCustomIdentifier);
        startActivityForResult(intent, MposUi.REQUEST_CODE_PAYMENT);
    }

    /**
     * Setting up the Pay Button
     */
    private void initializeMposUi() {
        mMposUi = MposUi.initialize(this, mProviderMode, mMerchantIdentifier, mMerchantSecretKey);
        mMposUi.getConfiguration().setAccessoryFamily(mAccessoryFamily);
        mMposUi.getConfiguration().setSummaryFeatures(EnumSet.of(MposUiConfiguration.SummaryFeature.SEND_RECEIPT_VIA_EMAIL));

        mMposUi.getConfiguration().getAppearance().setColorPrimaryDark(getResources().getColor(R.color.pa_payworks_blue_dark));
        mMposUi.getConfiguration().getAppearance().setColorPrimary(getResources().getColor(R.color.pa_payworks_blue));
        mMposUi.getConfiguration().getAppearance().setTextColorPrimary(Color.WHITE);
    }

    private void showError() {
        Log.e(TAG, "Error");
        Intent intent = new Intent(this, ErrorActivity.class);
        startActivity(intent);
    }

    /**
     * This is never called. We do it because it is the right thing to do.
     * Soon...we all must face a choice between what is right and what is easy.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MposUi.REQUEST_CODE_PAYMENT) {
            if (resultCode == MposUi.RESULT_CODE_APPROVED) {
                // Transaction was approved
                Toast.makeText(this, "Transaction approved", Toast.LENGTH_LONG).show();
            } else {
                // Card was declined, or transaction was aborted, or failed
                // (e.g. no internet or accessory not found)
                Toast.makeText(this, "Transaction was declined, aborted, or failed",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}