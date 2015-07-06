package sintef.android.emht.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

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

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
        objectMapper = new ObjectMapper();
        restAPIClient = new RestAPIClient();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.w(TAG, "onPerformSync");
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
}
