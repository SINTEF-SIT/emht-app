package sintef.android.emht.sync;

import android.accounts.AuthenticatorException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by iver on 7/6/15.
 */
public class RestAPIClient {

    private String authToken;
    private final String TAG = this.getClass().getSimpleName();
    private String serverUrl;

    public RestAPIClient(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public synchronized void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    String convertStreamToString(java.io.InputStream is) {
        if (is == null) return "";
        try {
            return new java.util.Scanner(is).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }

    public void post(final String endPoint, final String postData) throws Exception {
        URL url = new URL(serverUrl + endPoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(false);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        if (postData != null) connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        connection.setRequestProperty("Cookie", authToken);
        // all post data are of type json. if no data is to be sent, do not set the content-type
        if (postData != null) {
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                bw.write(postData);
                bw.flush();
            } finally {
                if (bw != null) bw.close();
            }
        }
        Log.w(TAG, convertStreamToString(connection.getErrorStream()));
        exceptionHandler(connection.getResponseCode());
    }


    public String get(String endPoint) throws Exception {
        Log.w(TAG, "trying to read url");
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(serverUrl + endPoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cookie", authToken);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(false);
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
            default:
                throw new Exception("Unknown exception");
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
