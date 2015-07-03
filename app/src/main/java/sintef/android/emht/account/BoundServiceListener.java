package sintef.android.emht.account;

/**
 * Created by iver on 27/06/15.
 */
public interface BoundServiceListener {
    void showGooglePlayServicesErrorDialog(int errorCode);
    void showAlarmTransmitComplete();
    void updateSensors();

}
