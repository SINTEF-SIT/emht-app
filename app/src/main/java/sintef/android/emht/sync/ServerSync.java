package sintef.android.emht.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.OnAccountsUpdateListener;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
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
import sintef.android.emht.account.BoundServiceListener;
import sintef.android.emht.events.NewAlarmEvent;
import sintef.android.emht.events.NewGcmAlarmEvent;
import sintef.android.emht.models.Alarm;
import sintef.android.emht.models.Patient;
import sintef.android.emht.models.SensorData;
import sintef.android.emht.utils.Constants;

/**
 * Created by iver on 24/06/15.
 */
public class ServerSync extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnAccountsUpdateListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private final String TAG = this.getClass().getSimpleName();
    private EventBus mEventBus;
    private AccountManager mAccountManager;
    private ObjectMapper objectMapper;
    private Account account;
    private String authTokenType;
    private String authToken;
    private int accountId;
//    private Timer alarmPollTimer;
    private Timer sensorPollTimer;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location currentBestLocation;
    IBinder mBinder = new LocalBinder();
    private BoundServiceListener mListener;
    private int googleApiErrorCode = 0;
    private SharedPreferences sharedPreferences;

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
        buildGoogleApiClient();

        authTokenType = Constants.AUTH_TOKEN_TYPE;
        if (mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length > 0) {
            account = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[Constants.ACCOUNT_INDEX];
            startSync();
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        updateGcmRegIdIfNeeded();

    }

    @SuppressWarnings("unused") // used by eventbus
    public void onEvent(NewGcmAlarmEvent newGcmAlarmEvent) {
        poll();
    }


    private void updateGcmRegIdIfNeeded() {

        //if (!sharedPreferences.getBoolean(Constants.pref_key_SENT_TOKEN_TO_SERVER, false)) {
            sendRegistrationIdToBackend(sharedPreferences.getString(Constants.pref_key_GCM_TOKEN, "NO_KEY"));
        //}

    }

    private void stopSync() {
        Log.w(TAG, "stopping timer for polling");
        //if (alarmPollTimer != null) alarmPollTimer.cancel();
        mGoogleApiClient.disconnect();
    }

    private void startSync() {
        Log.w(TAG, "starting timer for polling");
//        alarmPollTimer = new Timer();
//        alarmPollTimer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                poll();
//            }
//        }, 0, 30000); // poll every 30 seconds
        mGoogleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //accountId = intent.getExtras().getInt("account_id");
        //authTokenType = intent.getExtras().getString("auth_token_type");

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
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, "googleapierrorcode: " + connectionResult.getErrorCode());
        googleApiErrorCode = connectionResult.getErrorCode();
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
        if (!isBetterLocation(location, currentBestLocation)) return;

        final Map<String, Double> myLocation = new HashMap<String, Double>();

        myLocation.put("latitude", location.getLatitude());
        myLocation.put("longitude", location.getLongitude());

        new AsyncTask<Void, Void, Void>(

        ) {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    getExistingAccountAuthToken(account, authTokenType);
                    URL url = new URL(Constants.SERVER_URL + "/location/report");
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

    // from http://developer.android.com/guide/topics/location/strategies.html
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.w(TAG, "onSharedPreferenceChanged");
        if (key.equals(Constants.pref_key_GCM_TOKEN))
            sendRegistrationIdToBackend(sharedPreferences.getString(key, "NO_KEY"));
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

    public void poll() {
        Log.w(TAG, "polling server for alarms");
        new AsyncTask<Void, Alarm, Alarm>() {

            String json = null;

            @Override
            protected Alarm doInBackground(Void... params) {
                /* poll server for alarms. publish on eventbus */
                try {
                    json = readUrl(Constants.SERVER_URL + "/alarm/assignedToMe");
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
                            alarmObj.getAssessment().getNmi().save();
                            alarmObj.getAssessment().save();
                            alarmObj.getAttendant().save();
                            alarmObj.getMobileCareTaker().save();
                            alarmObj.save();
                            mEventBus.post(new NewAlarmEvent((alarmObj.getId())));
                            Log.w(TAG, "added new alarm to db");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
            if (acc.type.equals(Constants.ACCOUNT_TYPE)) {
                account = acc;
                stopSync();
                startSync();
            }
        }
    }

    public void addAlarmToTransmitQueue(Alarm alarm) {
        transmitAlarm(alarm);
        finishAlarm(alarm.getId());
        alarm.setFinished(true);
        alarm.setActive(false);
        alarm.save();
    }

    public void transmitAlarm(final Alarm alarm) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    getExistingAccountAuthToken(account, authTokenType);
                    URL url = new URL(Constants.SERVER_URL + "/alarm/saveAndFollowup");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json;charset=utf8");
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Cookie", authToken);

                    /* Visibility and filters required to remove Sugar ORM fields from models */
                    objectMapper.setVisibility(JsonMethod.ALL, JsonAutoDetect.Visibility.NONE);
                    objectMapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
                    objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
                    //objectMapper.registerModule(module);
                    SimpleBeanPropertyFilter sugarFilter = SimpleBeanPropertyFilter.serializeAllExcept("tableName");
                    FilterProvider filters = new SimpleFilterProvider().addFilter("sugarFilter", sugarFilter);
                    objectMapper.writer(filters).writeValue(new DataOutputStream(connection.getOutputStream()), alarm);
                    Log.w(TAG, objectMapper.writer(filters).writeValueAsString(alarm));

                    connection.connect();
                    if (connection.getResponseCode() != 200) {
                        invalidateAuthToken(account, authTokenType);
                        return false;
                    }
                    Log.w(TAG, Integer.toString(connection.getResponseCode()));
                    return true;
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
    }

    public void startSensorPolling() {
        Log.w(TAG, "starting sensor polling");
        sensorPollTimer = new Timer();
        sensorPollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateSensors(6L);
            }
        }, 0, 10000); // poll every 10 seconds
    }

    public void stopSensorPolling() { if (sensorPollTimer != null) sensorPollTimer.cancel(); }

    public void updateSensors(long patientId) {
        Log.w(TAG, "polling server for sensor datas");
        new AsyncTask<Long, Void, Void>() {

            String json = null;

            @Override
            protected Void doInBackground(Long... params) {
                /* poll server for sensor datas. publish on eventbus */
                try {
                    json = readUrl(Constants.SERVER_URL + "/component/" + Long.toString(params[0]));
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(patientId);
    }

    private void finishAlarm(final Long alarmId) {



        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    getExistingAccountAuthToken(account, authTokenType);
                    URL url = new URL(Constants.SERVER_URL + "/alarm/" + Long.toString(alarmId) + "/finish");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json;charset=utf8");
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Cookie", authToken);
                    connection.connect();
                    if (connection.getResponseCode() != 200) {
                        invalidateAuthToken(account, authTokenType);
                        return false;
                    }
                    return true;
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
    }

    public void sendRegistrationIdToBackend(final String token) {
        Log.w(TAG, "sending registration token");
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("gcmRegId", token);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    getExistingAccountAuthToken(account, authTokenType);
                    URL url = new URL(Constants.SERVER_URL + "/attendants/setGcmRegId");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json;charset=utf8");
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Cookie", authToken);
                    new DataOutputStream(connection.getOutputStream()).writeBytes(new JSONObject(parameters).toString());
                    //connection.connect();
                    Log.w(TAG, "regid response code: " + connection.getResponseCode());
                    if (connection.getResponseCode() != 200) {
                        invalidateAuthToken(account, authTokenType);
                        sendRegistrationIdToBackend(token);
                    }
                    sharedPreferences.edit().putBoolean(Constants.pref_key_SENT_TOKEN_TO_SERVER, true).apply();
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
    }
}
