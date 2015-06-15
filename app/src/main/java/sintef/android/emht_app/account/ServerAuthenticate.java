package sintef.android.emht_app.account;

import android.content.Context;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
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
        //String parameters = "username=iver&password=password";

        try {
            new URL("http://10.218.86.177:9000/logout").openConnection().connect();
            URL url = new URL("http://10.218.86.177:9000/login");
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
            List<HttpCookie> cookies = cm.getCookieStore().get(new URI("http://10.218.86.177:9000/login"));

            String session = null;

            for (HttpCookie cookie : cookies) {
                Log.w(TAG, cookie.toString());
                if (cookie.toString().startsWith("PLAY_SESSION")) session = cookie.toString();
            }

            Log.w(TAG, "session: " + session);

            if(session != null) {
                return session;
            }

            throw new IOException("unable to retrieve cookie");

        } catch (IOException e) {
            Log.w(TAG, e.toString());
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        return null;
    }
}
