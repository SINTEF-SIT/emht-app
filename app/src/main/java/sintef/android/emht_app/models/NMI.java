package sintef.android.emht_app.models;

import com.orm.SugarRecord;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by iver on 25/06/15.
 */
public class NMI extends SugarRecord<NMI> implements Serializable {

    @JsonProperty("id")
    public Long id;
    public Boolean conscious;
    public Boolean breathing;
    public Boolean movement;
    public Boolean standing;
    public Boolean talking;
}
