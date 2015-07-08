package sintef.android.emht.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import sintef.android.emht.events.NewAlarmEvent;
import sintef.android.emht.models.Alarm;
import sintef.android.emht.models.Patient;
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
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        if (restAPIClient == null) restAPIClient = new RestAPIClient(mAccountManager.getUserData(account, Constants.pref_key_SERVER_URL));
        try {
            Log.w(TAG, "onPerformSync");
            sendAlarms(account);
            updateAlarms();
            Log.w(TAG, "gcm reg id from syncadapter: " + PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.pref_key_GCM_TOKEN, "NO_KEY"));
            if (!PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.pref_key_SENT_TOKEN_TO_SERVER, false))
                sendGcmRegiId(account);
        } catch (AuthenticatorException e) {
            syncResult.stats.numAuthExceptions++;
            try {
                AccountManager.get(mContext).invalidateAuthToken(account.type, AccountManager.get(mContext).blockingGetAuthToken(account, Constants.AUTH_TOKEN_TYPE, true));
            } catch (OperationCanceledException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (AuthenticatorException e1) {
                e1.printStackTrace();
            }
        } catch (RestAPIClient.ServerErrorException e) {
            syncResult.stats.numIoExceptions++;
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void sendAlarms(Account account) throws Exception {
        List<Alarm> alarms = Alarm.find(Alarm.class, "add_to_upload_queue = ?", "1");
        restAPIClient.setAuthToken(mAccountManager.blockingGetAuthToken(account, Constants.AUTH_TOKEN_TYPE, true));
        for (Alarm alarm : alarms) {
                /* Visibility and filters required to remove Sugar ORM fields from models */
            objectMapper.setVisibility(JsonMethod.ALL, JsonAutoDetect.Visibility.NONE);
            objectMapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
            objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
            SimpleBeanPropertyFilter sugarFilter = SimpleBeanPropertyFilter.serializeAllExcept("tableName");
            FilterProvider filters = new SimpleFilterProvider().addFilter("sugarFilter", sugarFilter);
            String json = objectMapper.writer(filters).writeValueAsString(alarm);
            Log.w(TAG, "json upstream: " + json);

            Log.w(TAG, "posting to saveAndFollowup");
            restAPIClient.post("/alarm/saveAndFollowup", json);
            alarm.setFinished(true);
            alarm.save();
            Log.w(TAG, "posting to finish");
            restAPIClient.post("/alarm/" + Long.toString(alarm.getAlarmId()) + "/finish", null);

            alarm.delete();
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
                    if (alarmObj.getPatient() != null) alarmObj.getPatient().save();
                    alarmObj.getCallee().save();
                    alarmObj.getFieldAssessment().getNmi().save();
                    alarmObj.getFieldAssessment().save();
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
        String regId = PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.pref_key_GCM_TOKEN, null);
        if (regId == null) return;
        parameters.put("gcmRegId", regId);
        try {
            restAPIClient.setAuthToken(mAccountManager.blockingGetAuthToken(account, Constants.AUTH_TOKEN_TYPE, true));
            restAPIClient.post("/attendants/setGcmRegId", new JSONObject(parameters).toString());
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(Constants.pref_key_SENT_TOKEN_TO_SERVER, true).apply();
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}