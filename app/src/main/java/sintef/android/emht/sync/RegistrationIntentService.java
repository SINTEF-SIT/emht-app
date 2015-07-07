package sintef.android.emht.sync;

import android.accounts.AccountManager;
import android.app.DownloadManager;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.appdatasearch.GetRecentContextCall;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.maps.GoogleMap;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import sintef.android.emht.R;
import sintef.android.emht.events.NewSyncEvent;
import sintef.android.emht.utils.Constants;

/**
 * Created by iver on 7/5/15.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG="RegistrationIntentServ";

    private ServerSync mServerSync;
    private boolean mBound = false;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            synchronized (TAG) {
                // Initially a network call, to retrieve the token, subsequent calls are local.
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                Log.w(TAG, "GCM Registration Token: " + token);

                if (sharedPreferences.getString(Constants.pref_key_GCM_TOKEN, "").equals(token)) return;

                sharedPreferences.edit().putBoolean(Constants.pref_key_SENT_TOKEN_TO_SERVER, false).apply();
                sharedPreferences.edit().remove(Constants.pref_key_GCM_TOKEN).apply();
                sharedPreferences.edit().putString(Constants.pref_key_GCM_TOKEN, token).apply();
                EventBus.getDefault().post(new NewSyncEvent());

            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(Constants.pref_key_SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.intent_name_REGISTRATION_COMPLETE));
    }


}
