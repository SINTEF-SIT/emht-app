package sintef.android.emht_app.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.OnAccountsUpdateListener;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import sintef.android.emht_app.events.NewAlarmEvent;
import sintef.android.emht_app.models.Alarm;

/**
 * Created by iver on 24/06/15.
 */
public class ServerSync extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnAccountsUpdateListener {

    private final String TAG = this.getClass().getSimpleName();
    private EventBus mEventBus;
    private AccountManager mAccountManager;
    private ObjectMapper objectMapper;
    private Account account;
    private String authTokenType;
    private String authToken;
    private int accountId;
    private Timer poller;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "starting polling");
        mEventBus = EventBus.getDefault();
        objectMapper = new ObjectMapper();
        mAccountManager = AccountManager.get(getApplicationContext());
        mAccountManager.addOnAccountsUpdatedListener(this, null, false);
        buildGoogleApiClient();
    }

    private void stopSync() {
        Log.w(TAG, "stopping timer for polling");
        if (poller != null) poller.cancel();
        mGoogleApiClient.disconnect();
    }

    private void startSync() {
        Log.w(TAG, "starting timer for polling");
        poller = new Timer();
        poller.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                poll();
            }
        }, 5000, 30000); // poll every 30 seconds
        mGoogleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        accountId = intent.getExtras().getInt("account_id");
        authTokenType = intent.getExtras().getString("auth_token_type");
        if (mAccountManager.getAccountsByType("sintef.android.emht_app").length > 0) {
            account = mAccountManager.getAccountsByType("sintef.android.emht_app")[accountId];
            startSync();
        }
        return flags;
    }

    protected synchronized void buildGoogleApiClient() {
        Log.w(TAG, "building googleapiclient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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
                    getExistingAccountAuthToken(account, authTokenType);
                    URL url = new URL("http://129.241.105.197:9000/location/report");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json;charset=utf8");
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Cookie", authToken);
                    new DataOutputStream(connection.getOutputStream()).writeBytes(new JSONObject(myLocation).toString());
                    connection.connect();
                    if (connection.getResponseCode() != 200) {
                        invalidateAuthToken(account, authTokenType);
                        return null;
                    }
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void poll() {
        Log.w(TAG, "polling server for alarms");
        new AsyncTask<Void, Alarm, Alarm>() {

            String json = null;

            @Override
            protected Alarm doInBackground(Void... params) {
                /* poll server for alarms. publish on eventbus */
                try {
                    json = readUrl("http://129.241.105.197:9000/alarm/assignedToMe");
                    if (json == null) return null;
                    Log.w(TAG, "got: " + json);

                    JSONArray alarms = new JSONObject(json).getJSONArray("alarms");

                    for (int i = 0; i < alarms.length(); i++) {
                        JSONObject alarm = alarms.getJSONObject(i);
                        /* add alarm to db if it does not exist */
                        if (Alarm.find(Alarm.class, "alarm_id = ?", Long.toString(alarm.getLong("id"))).size() == 0) {
                            Alarm alarmObj = objectMapper.readValue(alarm.toString(), Alarm.class);
                            alarmObj.getPatient().save();
                            alarmObj.getCallee().save();
                            alarmObj.save();
                            mEventBus.post(new NewAlarmEvent((alarmObj.getId())));
                            Log.w(TAG, "added new alarm to db");
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, e.toString());
                }
                return null;
            }
        }.execute();
    }

    private String readUrl(String urlString) throws Exception {
        Log.w(TAG, "trying to read url");
        BufferedReader bufferedReader = null;
        try {
            getExistingAccountAuthToken(account, authTokenType);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", authToken);
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() != 200) {
                invalidateAuthToken(account, authTokenType);
                return null;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String json = bufferedReader.readLine();
            Log.w(TAG, "read this: " + json);
            return json;
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
        }
    }

    /**
     * Get the auth token for an existing account on the AccountManager
     * @param account
     * @param authTokenType
     */
    private void getExistingAccountAuthToken(Account account, String authTokenType) {
        //final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null, null);
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, true, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();
                    authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    Log.d("udinic", "GetToken Bundle is " + bnd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Invalidates the auth token for the account
     * @param account
     * @param authTokenType
     */
    private void invalidateAuthToken(final Account account, String authTokenType) {
        //final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null,null);
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, true, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();
                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    mAccountManager.invalidateAuthToken(account.type, authtoken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        for (Account acc : accounts) {
            if (acc.type.equals("sintef.android.emht_app")) {
                account = acc;
                stopSync();
                startSync();
            }
        }
    }
}
