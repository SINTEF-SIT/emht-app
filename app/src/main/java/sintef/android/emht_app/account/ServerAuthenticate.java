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
        String login_url = "http://10.218.86.177:9000/login";

        HttpURLConnection connection;
        String parameters = "username=" + username + "&password=" + password;

        try {
            URL url = new URL(mContext.getString(R.string.login_url_dev));
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");
            new DataOutputStream(connection.getOutputStream()).writeBytes(parameters);

            Map<String, List<String>> headerFields = connection.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("PLAY_SESSION");

            if(cookiesHeader != null)
            {
                Log.w(TAG, cookiesHeader.get(0));
                return cookiesHeader.get(0);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }
}
