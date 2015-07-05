package sintef.android.emht.sync;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by iver on 03/07/15.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {

    // Constants
    // Content provider authority
    public static final String AUTHORITY = "sintef.android.emht.datasync.provider";
    // Account type
    public static final String ACCOUNT_TYPE = "sintef.android.emht.datasync";
    // Account
    public static final String ACCOUNT = "default_account";
    // Incoming Intent key for extended data
    public static final String KEY_SYNC_REQUEST =
            "sintef.android.emht.datasync.KEY_SYNC_REQUEST";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get a GCM object instance
        GoogleCloudMessaging gcm =
                GoogleCloudMessaging.getInstance(context);
        // Get the type of GCM message
        String messageType = gcm.getMessageType(intent);

        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)
                &&
                intent.getBooleanExtra(KEY_SYNC_REQUEST, false)) {
            /*
             * Signal the framework to run your sync adapter. Assume that
             * app initialization has already created the account.
             */
//            ContentResolver.requestSync(ACCOUNT, AUTHORITY, null);
        }
    }
}
