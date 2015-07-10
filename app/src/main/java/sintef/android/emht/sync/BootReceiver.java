package sintef.android.emht.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by iver on 02/07/15.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent serverSync = new Intent(context, ServerSync.class);
        context.startService(serverSync);

    }
}
