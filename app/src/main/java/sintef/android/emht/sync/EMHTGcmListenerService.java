package sintef.android.emht.sync;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by iver on 7/5/15.
 */
public class EMHTGcmListenerService extends GcmListenerService {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.w(TAG, "GCM recieved message: " + data.getString("message"));
        // TODO: pull alarms on predefined message
    }
}
