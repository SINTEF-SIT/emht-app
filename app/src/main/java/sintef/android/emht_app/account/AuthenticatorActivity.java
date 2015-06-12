package sintef.android.emht_app.account;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import sintef.android.emht_app.R;

/**
 * Created by iver on 09/06/15.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    private final String TAG = this.getClass().getSimpleName();
    AccountManager mAccountManager;

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";
    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";
    public final static String PARAM_USER_PASS = "USER_PASS";
    private final int REQ_SIGNUP = 1;

    private String mAuthTokenType = "dummy access";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAccountManager = AccountManager.get(getBaseContext());
        String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);
        Log.w(TAG, "onCreate");

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        finishLogin(data);
    }

    private void submit() {

        final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        final String userName = ((TextView) findViewById(R.id.username)).getText().toString();
        final String userPass = ((TextView) findViewById(R.id.password)).getText().toString();

        new AsyncTask<String, Void, Intent>() {

            @Override
            protected Intent doInBackground(String... params) {
                Bundle data = new Bundle();
                String authToken = null;

                try {
                    Log.w(TAG, "trying to get authtoken");
                    authToken = AccountGeneral.getInstance(getApplicationContext()).mServerAuthenticate.userSignIn(userName, userPass, null);
                    Log.w(TAG, authToken);

                    data.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, "sintef.android.emht_app");
                    data.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                    data.putString(PARAM_USER_PASS, userPass);

                } catch (Exception e) {
                    if (e == null) Log.w(TAG, "exception is null");
                    Log.w(TAG, e.getMessage());
                    data.putString(KEY_ERROR_MESSAGE, e.getClass().getName());
                }
                final Intent result = new Intent();
                result.putExtras(data);
                return result;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
                    Toast.makeText(getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                } else {
                    finishLogin(intent);
                }
            }
        }.execute();


    }

    private void finishLogin(Intent intent) {
        Log.d(TAG, "finishLogin");

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d(TAG, "finishLogin > addAccountExplicitly");
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            Log.d(TAG,"finishLogin > setPassword");
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

}
