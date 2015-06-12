package sintef.android.emht_app.account;

import android.content.Context;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import sintef.android.emht_app.R;

/**
 * Created by iver on 10/06/15.
 */
public class ServerAuthenticate {

    Context mContext;
    private final String TAG = this.getClass().getSimpleName();

    public ServerAuthenticate(Context context) {
        this.mContext = context;
    }

    public String userSignIn(String username, String password, String authTokenType) {

        HttpURLConnection connection;
        String parameters = "username=" + username + "&password=" + password;

        try {
            //new URL("http://10.218.86.177:9000/logout").openConnection().connect();
            URL url = new URL("http://10.218.86.177:9000/login");
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");
            new DataOutputStream(connection.getOutputStream()).writeBytes(parameters);

            Map<String, List<String>> headerFields = connection.getHeaderFields();
            Log.w(TAG, "looking for cookie");

            for (List<String> val : headerFields.values()) {
                for (String str : val) {
                    Log.w(TAG, str);
                }
            }

            Log.w(TAG, "cookie: " + headerFields.get("Set-Cookie"));
            List<String> cookiesHeader = headerFields.get("PLAY_SESSION");

            if(cookiesHeader != null)
            {
                Log.w(TAG, cookiesHeader.get(0));
                return cookiesHeader.get(0);
            }

        } catch (IOException e) {
            Log.w(TAG, e.toString());
            e.printStackTrace();
        }


        return null;
    }
}
