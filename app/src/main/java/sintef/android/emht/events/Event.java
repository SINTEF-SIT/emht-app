package sintef.android.emht.events;

/**
 * Created by iver on 01/07/15.
 */
public abstract class Event {
    private long id;

    public Event(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
