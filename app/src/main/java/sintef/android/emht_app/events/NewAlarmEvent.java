package sintef.android.emht_app.events;

/**
 * Created by iver on 12/06/15.
 */
public class NewAlarmEvent {

    private long id;

    public NewAlarmEvent(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

}
