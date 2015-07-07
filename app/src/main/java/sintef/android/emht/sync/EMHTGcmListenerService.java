package sintef.android.emht.sync;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import de.greenrobot.event.EventBus;
import sintef.android.emht.MapsActivity;
import sintef.android.emht.R;
import sintef.android.emht.events.SyncEvent;
import sintef.android.emht.utils.Constants;

/**
 * Created by iver on 7/5/15.
 */
public class EMHTGcmListenerService extends GcmListenerService {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.w(TAG, "GCM recieved message: " + message);
        switch (message) {
            case (Constants.GCM_NEW_ALARM):
//                EventBus.getDefault().post(new NewGcmAlarmEvent());
                EventBus.getDefault().post(new SyncEvent());
                buildNewAlarmNotification();
        }
    }

    private void buildNewAlarmNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.gcm_new_alarm));
        Intent resultIntent = new Intent(this, MapsActivity.class);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MapsActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long[] vibratePattern = {0, 100, 1000};
        mBuilder.setVibrate(vibratePattern);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(Constants.GCM_NEW_ALARM_NOTIFICATION_ID, mBuilder.build());
    }
}
