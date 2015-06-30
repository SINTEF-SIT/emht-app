package sintef.android.emht_app.models;

import com.orm.SugarRecord;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonFilter;

import java.io.Serializable;

/**
 * Created by iver on 25/06/15.
 */

@JsonFilter("sugarFilter")
public class Assessment extends SugarRecord<Assessment> implements Serializable {

    @JsonProperty("id")
    public Long assessmentId;
    public NMI nmi;
    public boolean sensorsChecked;
    public boolean patientInformationChecked;

    public Assessment() {}

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public NMI getNmi() {
        return nmi;
    }

    public void setNmi(NMI nmi) {
        this.nmi = nmi;
    }

    public boolean isSensorsChecked() {
        return sensorsChecked;
    }

    public void setSensorsChecked(boolean sensorsChecked) {
        this.sensorsChecked = sensorsChecked;
    }

    public boolean isPatientInformationChecked() {
        return patientInformationChecked;
    }

    public void setPatientInformationChecked(boolean patientInformationChecked) {
        this.patientInformationChecked = patientInformationChecked;
    }
}
