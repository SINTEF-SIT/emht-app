package sintef.android.emht.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import de.greenrobot.event.EventBus;
import sintef.android.emht.events.NetworkChangeEvent;
import sintef.android.emht.utils.Helper;

/**
 * Created by iver on 07/07/15.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("NCR", "networkchange");
        EventBus.getDefault().post(new NetworkChangeEvent(Helper.isConnected(context)));
    }


}
