package sintef.android.emht_app.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by iver on 09/06/15.
 */
public class AuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new AccountAuthenticator(this).getIBinder();
    }
}
