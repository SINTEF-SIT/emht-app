package sintef.android.emht.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import sintef.android.emht.events.NewAlarmEvent;
import sintef.android.emht.models.Alarm;
import sintef.android.emht.utils.Constants;

/**
 * Created by iver on 03/07/15.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private final AccountManager mAccountManager;
    private ObjectMapper objectMapper;
    private final String TAG = this.getClass().getSimpleName();
    private RestAPIClient restAPIClient;
    private Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.mContext = context;
        mAccountManager = AccountManager.get(context);
        objectMapper = new ObjectMapper();
        restAPIClient = new RestAPIClient();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.w(TAG, "onPerformSync");
        sendAlarms(account);
        updateAlarms();
        Log.w(TAG, "gcm reg id from syncadapter: " + PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.pref_key_GCM_TOKEN, "NO_KEY"));
        if (!PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.pref_key_SENT_TOKEN_TO_SERVER, false)) sendGcmRegiId(account);
    }

    private void sendAlarms(Account account) {
        List<Alarm> alarms = Alarm.find(Alarm.class, "finished = ?", "1");
        try {
            restAPIClient.setAuthToken(mAccountManager.blockingGetAuthToken(account, Constants.ACCOUNT_TYPE, true));
            for (Alarm alarm : alarms) {
                /* Visibility and filters required to remove Sugar ORM fields from models */
                objectMapper.setVisibility(JsonMethod.ALL, JsonAutoDetect.Visibility.NONE);
                objectMapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
                objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
                SimpleBeanPropertyFilter sugarFilter = SimpleBeanPropertyFilter.serializeAllExcept("tableName");
                FilterProvider filters = new SimpleFilterProvider().addFilter("sugarFilter", sugarFilter);
                String json = objectMapper.writer(filters).writeValueAsString(alarm);
                Log.w(TAG, "json upstream: " + json);

                restAPIClient.post("/alarm/saveAndFollowup", json);
                restAPIClient.post("/alarm/" + Long.toString(alarm.getAlarmId()) + "/finish", null);

                alarm.delete();

            }
        } catch (OperationCanceledException e1) {
            e1.printStackTrace();
        } catch (AuthenticatorException e1) {
            e1.printStackTrace();
        } catch (ProtocolException e1) {
            e1.printStackTrace();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void updateAlarms() {
        Log.w(TAG, "polling server for alarms");
            String json = null;
                try {
                    json = restAPIClient.get("/alarm/assignedToMe");
                    if (json == null) return;
                    JSONArray alarms = new JSONObject(json).getJSONArray("alarms");
                    for (int i = 0; i < alarms.length(); i++) {
                        JSONObject alarm = alarms.getJSONObject(i);
                        /* skip alarm if it's marked as finished */
                        if (alarm.getBoolean("finished")) continue;
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
                            EventBus.getDefault().post(new NewAlarmEvent((alarmObj.getId())));
                            Log.w(TAG, "added new alarm to db");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
    }

    private void sendGcmRegiId(Account account) {
        Log.w(TAG, "syncadapter sending gcm reg id");
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("gcmRegId", PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.pref_key_GCM_TOKEN, "NO_KEY"));
        try {
            restAPIClient.setAuthToken(mAccountManager.blockingGetAuthToken(account, Constants.ACCOUNT_TYPE, true));
            restAPIClient.post("/attendants/setGcmRegId", new JSONObject(parameters).toString());
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(Constants.pref_key_SENT_TOKEN_TO_SERVER, true).apply();
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }
    }
}