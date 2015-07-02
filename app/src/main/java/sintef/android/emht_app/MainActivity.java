package sintef.android.emht_app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.List;

import sintef.android.emht_app.models.Alarm;


public class MainActivity extends FragmentActivity {

    private final String TAG = this.getClass().getSimpleName();
    private String ACCOUNT_TYPE = "sintef.android.emht_app";
    private AccountManager mAccountManager;
    private AlertDialog mAlertDialog;
    private String authToken;
    //private Intent incidientActivity;
    //private Location previousLocation;
    //private Intent dashboardActivity;
    private Intent mapsActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAccountManager = AccountManager.get(this);

        Log.w(TAG, "onCreate");

        checkGooglePlayServices(this);

        //dashboardActivity = new Intent(this, DashboardActivity.class);
        //dashboardActivity.putExtra("account_id", 0);
        //dashboardActivity.putExtra("auth_token_type", "dummytoken");

        mapsActivity = new Intent(this, MapsActivity.class);
        mapsActivity.putExtra("account_id", 0);
        mapsActivity.putExtra("auth_token_type", "dummytoken");

        /* Retrieve first alarm from database.
           If no alarms are available (i.e. first login) start the app without an alarm selected  */
        //List<Alarm> firstAlarm = Alarm.findWithQuery(Alarm.class, "select * from alarm order by ROWID asc limit 1");
        //if (firstAlarm.size() > 0) dashboardActivity.putExtra("alarm_id", firstAlarm.get(0).getId());

        /* check if there exists account(s) */
        switch (mAccountManager.getAccountsByType(ACCOUNT_TYPE).length) {
            case 0:
                addNewAccount();
                break;
            default:
                //startActivity(dashboardActivity);
                startActivity(mapsActivity);
                break;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkGooglePlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 9000).show();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void addNewAccount() {
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(ACCOUNT_TYPE, null, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    Log.w(TAG, "Account was created");
                    Log.d(TAG, "AddNewAccount Bundle is " + bnd);
                    startActivity(mapsActivity);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w(TAG, e.getMessage());
                }
            }
        }, null);
    }



    private void showAccountPicker(final String authTokenType) {
        final Account availableAccounts[] = mAccountManager.getAccountsByType(ACCOUNT_TYPE);

        if (availableAccounts.length == 0) {
            Toast.makeText(this, "No accounts", Toast.LENGTH_SHORT).show();
        } else {
            String name[] = new String[availableAccounts.length];
            for (int i = 0; i < availableAccounts.length; i++) {
                name[i] = availableAccounts[i].name;
            }

            // Account picker
            mAlertDialog = new AlertDialog.Builder(this).setTitle("Pick account").setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, name), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getExistingAccountAuthToken(availableAccounts[which], authTokenType);
                }
            }).create();
            mAlertDialog.show();
        }
    }

    /**
     * Get the auth token for an existing account on the AccountManager
     * @param account
     * @param authTokenType
     */
    private void getExistingAccountAuthToken(Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();

                    authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    showMessage((authToken != null) ? "SUCCESS!\ntoken: " + authToken : "FAIL");
                    Log.d("udinic", "GetToken Bundle is " + bnd);
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }).start();

    }

    private void showMessage(final String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
