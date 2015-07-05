package sintef.android.emht.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by iver on 09/06/15.
 */
public class AuthenticatorService extends Service {

    private AccountAuthenticator mAccountAuthenticator;

    @Override
    public void onCreate() {
        mAccountAuthenticator = new AccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAccountAuthenticator.getIBinder();
    }
}
