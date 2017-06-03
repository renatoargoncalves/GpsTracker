package com.sow.gpstrackerpro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sow.gpstrackerpro.application.MyApplication;
import com.sow.gpstrackerpro.classes.Log;
import com.sow.gpstrackerpro.inappbilling.IabHelper;
import com.sow.gpstrackerpro.inappbilling.IabResult;
import com.sow.gpstrackerpro.inappbilling.Inventory;
import com.sow.gpstrackerpro.inappbilling.Purchase;

import static android.R.attr.id;

public class InAppBillingActivity extends AppCompatActivity {

    private final static String TAG = "InAppBillingActivity";
    private Button buttonBuy;
    private IabHelper mHelper;
    private static final String ITEM_SKU = "com.sow.locator.premium";
    private Toolbar toolbar;
    private MyApplication myApplication;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_billing);

        myApplication = (MyApplication) getApplicationContext();

        toolbar = (Toolbar) findViewById(R.id.toolbar_inapp_billing);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.become_premium));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqB8AWAlUfPCfdteNia2WSM9v+FeiI2JaR22g2pOj50ONppgIr9l3mbWTuaTQ85zFe7DOjtF15vyO/G36Ux0Vbn6bD9uMuBKan8JwkvWMIQtZukOZ0zSucrmuQIb8aooHjDAykumiizbjMJgOm/d2W5eEQ1n0OlGoOmUrGzNaV4u8V7piTQmv8/x/RGqTiwnBY9dFKaR24DDn9V76QKl8bN8aWBe1q2UECfoofYQpZLfKnverCRXbkqRwjeZwtDe41vLWK1tcXG2msyJeN0OpdQdHt4HkgRqLta9vAQsqfCeZ5NcBo6AKuqR+mUP9Sb2NJ7dtPDtrq4FR8u1AsPTEIQIDAQAB";
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Toast.makeText(InAppBillingActivity.this, "In app billing failed", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Setup billing success!");
                }
            }
        });

        buttonBuy = (Button) findViewById(R.id.buttonBuy);
        buttonBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHelper.launchPurchaseFlow(InAppBillingActivity.this, ITEM_SKU, 10001, mPurchaseFinishedListener, "myPurchaseToken");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                Log.i(TAG, "mPurchaseFinishedListener: message: " + result.getMessage() + ", response: " + result.getResponse());
                if (result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED)
                    Toast.makeText(InAppBillingActivity.this, getString(R.string.item_already_owned), Toast.LENGTH_SHORT).show();
                else {
                    Log.e(TAG, "mPurchaseFinishedListener: message: " + result.getMessage() + ", response: " + result.getResponse());
                    Toast.makeText(InAppBillingActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
                return;
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                Log.i(TAG, "mPurchaseFinishedListener: success, will consume item");
                consumeItem();
            }
        }
    };

    private void consumeItem() {
        Log.i(TAG, "consumeItem");
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {

            if (result.isFailure()) {
                Log.i(TAG, "mReceivedInventoryListener: failure");
            } else {
                Log.i(TAG, "mReceivedInventoryListener: success");
                mHelper.consumeAsync(inv.getPurchase(ITEM_SKU), mConsumeFinishedListener);
            }

        }
    };


    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        @Override
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                Log.i(TAG, "mConsumeFinishedListener: success, will become premium");
                becomePremium();
            } else if (result.isFailure()){
                Log.i(TAG, "mConsumeFinishedListener: failure");
                Toast.makeText(InAppBillingActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    private void becomePremium() {
        Log.w(TAG, "becomePremium()");
        try {
            DatabaseReference dbRef_premium = database.getReference("/users/" + myApplication.getFirebaseUser().getEmail().replace(".", "!") + "/premium/");
            dbRef_premium.setValue(true, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.e(TAG, "becomePremium NOT executed:" + databaseError.getMessage());
                        Toast.makeText(InAppBillingActivity.this, getString(R.string.purchase_not_completed), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "becomePremium success! Will start activity PurchaseComplete");
                        startActivity(new Intent(InAppBillingActivity.this, PurchaseCompletedActivity.class));
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "becomePremium: " + e.getMessage());
        }
    }

}
