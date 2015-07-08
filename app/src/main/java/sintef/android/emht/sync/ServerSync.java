package sintef.android.emht.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.OnAccountsUpdateListener;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import sintef.android.emht.account.BoundServiceListener;
import sintef.android.emht.events.NetworkChangeEvent;
import sintef.android.emht.events.NewGcmAlarmEvent;
import sintef.android.emht.events.SyncEvent;
import sintef.android.emht.models.Patient;
import sintef.android.emht.models.SensorData;
import sintef.android.emht.utils.Constants;
import sintef.android.emht.utils.Helper;

/**
 * Created by iver on 24/06/15.
 */
public class ServerSync extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnAccountsUpdateListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private final String TAG = this.getClass().getSimpleName();
    private EventBus mEventBus;
    private AccountManager mAccountManager;
    private ObjectMapper objectMapper;
    private Account account;
    private String authTokenType;
    private String authToken;
    private int accountId;
    private Timer sensorPollTimer;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location currentBestLocation;
    IBinder mBinder = new LocalBinder();
    private BoundServiceListener mListener;
    private int googleApiErrorCode = 0;
    private SharedPreferences sharedPreferences;
    private static final Object authTokenLock = new Object();
    private RestAPIClient restAPIClient;
    private boolean hasNetworkConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "starting polling");
        mEventBus = EventBus.getDefault();
        mEventBus.register(this);
        objectMapper = new ObjectMapper();
        objectMapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        mAccountManager = AccountManager.get(getApplicationContext());
        mAccountManager.addOnAccountsUpdatedListener(this, null, false);
        hasNetworkConnection = Helper.isConnected(this);
        buildGoogleApiClient();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        authTokenType = Constants.AUTH_TOKEN_TYPE;

        if (mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length > 0) {
            account = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[Constants.ACCOUNT_INDEX];
            restAPIClient = new RestAPIClient(mAccountManager.getUserData(account, Constants.pref_key_SERVER_URL));
            if (hasNetworkConnection) startSync();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    private void stopSync() {
        mGoogleApiClient.disconnect();
    }

    private void startSync() {
        mGoogleApiClient.connect();
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
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, "googleapierrorcode: " + connectionResult.getErrorCode());
        googleApiErrorCode = connectionResult.getErrorCode();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (Helper.isBetterLocation(location, currentBestLocation)) {
            Log.w(TAG, "location changed");
            currentBestLocation = location;
            sendLocationData(location);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Does not get triggered for some reason. Solved by other means. Can be removed.
        Log.w(TAG, "onSharedPreferenceChanged");
        if (key.equals(Constants.pref_key_GCM_TOKEN))
            startSyncRequest();
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        for (Account acc : accounts) {
            if (acc.type.equals(Constants.ACCOUNT_TYPE)) {
                account = acc;
                stopSync();
                startSync();
            }
        }
    }

    @SuppressWarnings("unused") // used by eventbus
    public void onEvent(NewGcmAlarmEvent newGcmAlarmEvent) {
    }

    @SuppressWarnings("unused")
    public void onEvent(NetworkChangeEvent networkChangeEvent) {
        Log.w(TAG, "event networkchangeevent");
        if (this.hasNetworkConnection == networkChangeEvent.isConnected()) return;
        this.hasNetworkConnection = networkChangeEvent.isConnected();
        if (hasNetworkConnection && !mGoogleApiClient.isConnected()) mGoogleApiClient.connect();
        else if (!hasNetworkConnection && mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();
    }

    @SuppressWarnings("unused")
    public void onEvent(SyncEvent newSyncEvent) {
        startSyncRequest();
    }

    private void startSyncRequest() {
        Account account = AccountManager.get(this).getAccountsByType(Constants.ACCOUNT_TYPE)[0];

        Bundle syncBundle = new Bundle();
        syncBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        syncBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

//        SyncRequest.Builder mBuilder = new SyncRequest.Builder()
//                .setExpedited(true) // prioritize this sync
//                .setSyncAdapter(account, Constants.PROVIDER_AUTHORITIES)
//                .setManual(true)
//                .syncOnce()
//                .setExtras(new Bundle()); // Bundle mandatory to build request

        ContentResolver.setSyncAutomatically(account, Constants.PROVIDER_AUTHORITIES, true);
//        ContentResolver.requestSync(mBuilder.build());
        ContentResolver.requestSync(account, Constants.PROVIDER_AUTHORITIES, syncBundle);
    }

    public class LocalBinder extends Binder {
        public ServerSync getService() {
            return ServerSync.this;
        }
        public void setListener(BoundServiceListener listener) {
            mListener = listener;
            Log.w(TAG, "setListener, error code is: " + googleApiErrorCode);
            if (googleApiErrorCode > 0) mListener.showGooglePlayServicesErrorDialog(googleApiErrorCode);
        }
    }

    public void startSensorPolling() {
        Log.w(TAG, "starting sensor polling");
        sensorPollTimer = new Timer();
        sensorPollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateSensors(6L);
            }
        }, 0, 10000); // updateAlarms every 10 seconds
    }

    public void stopSensorPolling() { if (sensorPollTimer != null) sensorPollTimer.cancel(); }

    public void updateSensors(long patientId) {
        if (!hasNetworkConnection) return;
        Log.w(TAG, "polling server for sensor datas");
        new AsyncTask<Long, Void, Void>() {
            String json = null;
            @Override
            protected Void doInBackground(Long... params) {
                /* poll server for sensor datas. publish on eventbus */
                try {
                    json = restAPIClient.get("/component/" + Long.toString(params[0]));
                    if (json == null) return null;
                    Log.w(TAG, "got: " + json);

                    JSONArray readings = new JSONObject(json).getJSONArray("readings");

                    for (int i = 0; i < readings.length(); i++) {
                        JSONObject reading = readings.getJSONObject(i);
                        /* add reading to db if it does not exist */
                        if (SensorData.find(SensorData.class, "sensor_data_id = ?", Long.toString(reading.getLong("id"))).size() == 0) {
                            SensorData sensorData = objectMapper.readValue(reading.toString(), SensorData.class);
                            sensorData.setPatient(Patient.findById(Patient.class, params[0]));
                            sensorData.save();
                            mEventBus.post(sensorData);
                            Log.w(TAG, "added new sensor data to db");
                        }
                    }
                } catch (RestAPIClient.BadRequestException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(patientId);
    }

    private void sendLocationData(Location location) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, true, null, null);
        final Map<String, Double> parameters = new HashMap<>();
        parameters.put("latitude", location.getLatitude());
        parameters.put("longitude", location.getLongitude());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();
                    restAPIClient.setAuthToken(bnd.getString(AccountManager.KEY_AUTHTOKEN));
                    restAPIClient.post("/location/report", new JSONObject(parameters).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Log.w(TAG, "current location: " + location.getLatitude() + ", " + location.getLongitude());
    }
}
