package sintef.android.emht_app.models;

import com.orm.SugarRecord;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by iver on 25/06/15.
 */
public class Assessment extends SugarRecord<Assessment> implements Serializable {

    @JsonProperty("id")
    public Long assessmentId;
    public NMI nmi;
    public boolean sensorsChecked;
    public boolean patientInformationChecked;

}
