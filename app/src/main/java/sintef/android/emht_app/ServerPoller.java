package sintef.android.emht_app;

import android.accounts.AccountManager;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
    private String authToken;

    public ServerPoller(String authToken) {
        Log.w(TAG, "starting polling");
        mEventBus = EventBus.getDefault();
        this.authToken = authToken;
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                poll();
            }
        }, 5000, 30000); // poll every 30 seconds
    }

    private void poll() {
        Log.w(TAG, "polling server for alarms");
        new AsyncTask<Void, Alarm, Alarm>() {

            String json = null;

            @Override
            protected Alarm doInBackground(Void... params) {
                /* poll server for alarms. publish on eventbus */
                try {
                    json = readUrl("http://10.218.86.177:9000/alarms/assignedToMe");
                    if (json == null) return null;
                    Log.w(TAG, "got: " + json);

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
                                false, //alarm.getBoolean("expired"),
                                null,
                                alarm.getString("alarmLog"),
                                alarm.getString("notes"),
                                null
                        ));
                        Log.w(TAG, alarm.getString("type"));
                    }

                } catch (Exception e) {
                    Log.w(TAG, e.toString());
                }
                return null;
            }
        }.execute();
    }

    private String readUrl(String urlString) throws Exception {
        Log.w(TAG, "trying to read url");
        BufferedReader bufferedReader = null;
        try {
            return "{\"userId\":2,\"username\":\"iver\",\"role\":3,\"alarms\":[{\"id\":2,\"occuranceAddress\":null,\"alarmLog\":null,\"notes\":null,\"type\":\"safety_alarm\",\"openingTime\":\"Mon Sep 15 23:02:57 CEST 2014\",\"dispatchingTime\":null,\"closingTime\":null,\"callee\":{\"id\":1,\"name\":\"Berit Nilsen\",\"phoneNumber\":\"91105432\",\"address\":\"Nedre Møllenberg gt. 44\"},\"patient\":{\"id\":6,\"name\":\"Berit Nilsen\",\"persoNumber\":\"05033326826\",\"phoneNumber\":\"91105432\",\"address\":\"Nedre Møllenberg gt. 44\",\"age\":81}}]}";
            /*
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", authToken);
            connection.setRequestMethod("GET");
            connection.connect();

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String json = bufferedReader.readLine();
            Log.w(TAG, "read this: " + json);
            return json;
            */
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
        }
    }

}
