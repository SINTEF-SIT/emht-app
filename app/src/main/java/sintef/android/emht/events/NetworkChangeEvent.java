package sintef.android.emht.events;

/**
 * Created by iver on 07/07/15.
 */
public class NetworkChangeEvent {
    private boolean connected;
    public NetworkChangeEvent(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }
}
