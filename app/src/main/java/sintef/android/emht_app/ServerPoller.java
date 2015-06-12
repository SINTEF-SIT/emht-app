package sintef.android.emht_app;

import android.accounts.AccountManager;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import sintef.android.emht_app.models.Alarm;

/**
 * Created by iver on 12/06/15.
 */
public class ServerPoller {

    private final String TAG = this.getClass().getSimpleName();
    private EventBus mEventBus;
    private AccountManager mAccountManager;
    private static String authToken;

    public ServerPoller(String authToken) {
        mEventBus = new EventBus();
        this.authToken = authToken;
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                poll();
            }
        }, 0, 30000); // poll every 30 seconds
    }

    private void poll() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                /* poll server for alarms. publish on eventbus */
                String json = null;
                try {
                    json = readUrl("10.218.86.177:9000/alarms/assignedToMe");
                    if (json != null) return null;

                    JSONArray alarms = new JSONObject(json).getJSONArray("alarms");

                    for (int i = 0; i < alarms.length(); i++) {
                        JSONObject alarm = alarms.getJSONObject(i);
                        /* TODO: check if alarms exist */

                        mEventBus.post(new Alarm(
                                alarm.getLong("id"),
                                alarm.getString("type"),
                                null, // callee
                                null,
                                null,
                                null,
                                alarm.getString("occuranceAddress"),
                                alarm.getBoolean("expired"),
                                null,
                                alarm.getString("alarmLog"),
                                alarm.getString("notes"),
                                null
                        ));
                    }


                } catch (Exception e) {
                    Log.w(TAG, e.toString());
                }
                return null;
            }
        };
    }

    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Cookie", "PLAY_SESSION=" + authToken);
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

}
