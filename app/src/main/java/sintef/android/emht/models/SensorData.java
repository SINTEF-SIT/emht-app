package sintef.android.emht.models;

import com.orm.SugarRecord;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by iver on 01/07/15.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class SensorData extends SugarRecord<SensorData> implements Serializable{

    @JsonProperty("id")
    private Long sensorDataId;
    private String readingType;
    private Date date;
    private Double value;
    private Patient patient;

    public SensorData() {}

    public long getSensorDataId() {
        return sensorDataId;
    }

    public void setSensorDataId(long sensorDataId) {
        this.sensorDataId = sensorDataId;
    }

    public String getReadingType() {
        return readingType;
    }

    public void setReadingType(String readingType) {
        this.readingType = readingType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}
