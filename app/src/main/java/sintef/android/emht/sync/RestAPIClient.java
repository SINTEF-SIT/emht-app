package sintef.android.emht.sync;

import android.accounts.AuthenticatorException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import sintef.android.emht.utils.Constants;

/**
 * Created by iver on 7/6/15.
 */
public class RestAPIClient {

    private String authToken;
    private final String TAG = this.getClass().getSimpleName();

    public RestAPIClient() {
    }

    public synchronized void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void post(final String endPoint, final String postData) {
        try {
            URL url = new URL(Constants.SERVER_URL + endPoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(false);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf8");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Cookie", null);
            if (postData != null) new DataOutputStream(connection.getOutputStream()).writeBytes(postData);
            exceptionHandler(connection.getResponseCode());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String get(String endPoint) throws Exception {
        Log.w(TAG, "trying to read url");
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(Constants.SERVER_URL + endPoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", authToken);
            connection.setRequestMethod("GET");
            connection.connect();
            exceptionHandler(connection.getResponseCode());
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String json = bufferedReader.readLine();
            Log.w(TAG, "read this: " + json);
            return json;
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
        }
    }

    private void exceptionHandler(int responseCode) throws Exception {
        Log.w(TAG, "response code: " + responseCode);
        switch (responseCode) {
            case (303):
                throw new AuthenticatorException("Not logged in");
            case (400):
                throw new BadRequestException("Bad request");
            case (403):
                throw new AuthenticatorException("Forbidden");
            case (500):
                throw new ServerErrorException("Server is down");
        }
    }

    public class BadRequestException extends Exception {
        public BadRequestException(String detailMessage) {
            super(detailMessage);
        }
    }

    public class ServerErrorException extends Exception {
        public ServerErrorException(String detailMessage) {
            super(detailMessage);
        }
    }
}
