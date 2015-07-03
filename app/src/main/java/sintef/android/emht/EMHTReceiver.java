package sintef.android.emht;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import sintef.android.emht.sync.ServerSync;

/**
 * Created by iver on 02/07/15.
 */
public class EMHTReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent serverSync = new Intent(context, ServerSync.class);
        context.startService(serverSync);

    }
}
