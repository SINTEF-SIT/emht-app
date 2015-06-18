package sintef.android.emht_app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final String TAG = this.getClass().getSimpleName();
    private String ACCOUNT_TYPE = "sintef.android.emht_app";
    private AccountManager mAccountManager;
    private AlertDialog mAlertDialog;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private String authToken;
    //private Location previousLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAccountManager = AccountManager.get(this);

        Log.w(TAG, "onCreate");


        /* setup location service */
        buildGoogleApiClient();


        /* check if there exists account(s) */
        switch (mAccountManager.getAccountsByType(ACCOUNT_TYPE).length) {
            case 0:
                addNewAccount();
                break;
            case 1:
                getExistingAccountAuthToken(mAccountManager.getAccountsByType(ACCOUNT_TYPE)[0], "dummytoken");
                break;
            default:
                /* there exists more than one account. allow user to select account */
                showAccountPicker("dummytoken");
                break;
        }
        mGoogleApiClient.connect();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG, "onResume");
        /* check if user is logged in. proceed to content */
        /*
        Intent dashboard = new Intent(this, DashboardActivity.class);
        dashboard.putExtra("username", "dummy");
        startActivity(dashboard);
        */
        Intent alarmactivity = new Intent(this, AlarmActivity.class);
        startActivity(alarmactivity);
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

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w(TAG, e.getMessage());
                }
            }
        }, null);
    }

    protected synchronized void buildGoogleApiClient() {
        Log.w(TAG, "building googleapiclient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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
                    new ServerPoller(authToken);
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

    @Override
    public void onConnected(Bundle bundle) {
        Log.w(TAG, "location connected");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        /*
        if (previousLocation == null) previousLocation = location;
        if (Math.floor(location.getLongitude()/1000) ==
            Math.floor(previousLocation.getLongitude()/1000) &&
            Math.floor(location.getLatitude() / 1000) ==
                    Math.floor(previousLocation.getLatitude()/1000)) return;
        previousLocation = location;
        */
        Log.w(TAG, "location changed");

        final Map<String, Double> myLocation = new HashMap<String, Double>();

        myLocation.put("latitude", location.getLatitude());
        myLocation.put("longitude", location.getLongitude());

        new AsyncTask<Void, Void, Void>(

        ) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL("http://10.218.86.177:9000/location/report");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json;charset=utf8");
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Cookie", authToken);
                    new DataOutputStream(connection.getOutputStream()).writeBytes(new JSONObject(myLocation).toString());
                    connection.connect();
                    Log.w(TAG, Integer.toString(connection.getResponseCode()));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();

        Log.w(TAG, "current location: " + location.getLatitude() + ", " + location.getLongitude());
        Log.w(TAG, new JSONObject(myLocation).toString());
    }
}
