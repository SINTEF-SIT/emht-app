package sintef.android.emht.events;

/**
 * Created by iver on 06/07/15.
 */
public class NewGcmRegistrationIdEvent {
    private String token;
    public NewGcmRegistrationIdEvent(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }
}
