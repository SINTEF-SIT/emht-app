package sintef.android.emht_app.account;

import android.content.Context;

/**
 * Created by iver on 10/06/15.
 */
public class AccountGeneral {

    private static AccountGeneral instance;
    private static Context mContext;

    private AccountGeneral(Context context) {
        this.mContext = context;
    }

    public static synchronized AccountGeneral getInstance(Context context) {
        if (instance == null) {
            instance = new AccountGeneral(context.getApplicationContext());
        }

        return instance;
    }

    public static final ServerAuthenticate mServerAuthenticate = new ServerAuthenticate(mContext);
}
