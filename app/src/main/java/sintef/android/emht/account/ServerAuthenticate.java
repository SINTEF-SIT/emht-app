package sintef.android.emht.account;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import sintef.android.emht.utils.Constants;

/**
 * Created by iver on 10/06/15.
 */
public class ServerAuthenticate {

    Context mContext;
    private final String TAG = this.getClass().getSimpleName();
    private String serverUrl;

    public ServerAuthenticate(Context context) {
        this.mContext = context;
    }

    public String userSignIn(String username, String password, String authTokenType, String serverUrl) throws IOException, URISyntaxException {

        HttpURLConnection connection;
        String parameters = "username=" + username + "&password=" + password;

        new URL(serverUrl + "/logout").openConnection().connect();
        URL url = new URL(serverUrl + "/login");
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestMethod("POST");
        connection.setInstanceFollowRedirects(true);
        HttpURLConnection.setFollowRedirects(true);
        new DataOutputStream(connection.getOutputStream()).writeBytes(parameters);

        connection.getHeaderFields();
        Log.w(TAG, "looking for cookie");
        CookieManager cm = (CookieManager) CookieHandler.getDefault();
        List<HttpCookie> cookies = cm.getCookieStore().get(new URI(serverUrl + "/login"));

        String session = null;

        for (HttpCookie cookie : cookies) {
            Log.w(TAG, cookie.toString());
            if (cookie.toString().startsWith("PLAY_SESSION")) session = cookie.toString();
        }

        Log.w(TAG, "session: " + session);

        if(session != null) {
            return session;
        }
        return null;
    }
}
