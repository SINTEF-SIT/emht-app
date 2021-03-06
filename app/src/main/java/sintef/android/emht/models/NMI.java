package sintef.android.emht.models;

import com.orm.SugarRecord;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonFilter;

import java.io.Serializable;

/**
 * Created by iver on 25/06/15.
 */

@JsonFilter("sugarFilter")
public class NMI extends SugarRecord<NMI> implements Serializable {

    @JsonProperty("id")
    private Long nmiId;
    private Boolean conscious;
    private Boolean breathing;
    private Boolean movement;
    private Boolean standing;
    private Boolean talking;

    public NMI() {}

    public Long getNmiId() {
        return nmiId;
    }

    public void setNmiId(Long nmiId) {
        this.nmiId = nmiId;
    }

    public Boolean isConscious() {
        return conscious;
    }

    public void setConscious(Boolean conscious) {
        this.conscious = conscious;
    }

    public Boolean isBreathing() {
        return breathing;
    }

    public void setBreathing(Boolean breathing) {
        this.breathing = breathing;
    }

    public Boolean isMovement() {
        return movement;
    }

    public void setMovement(Boolean movement) {
        this.movement = movement;
    }

    public Boolean isStanding() {
        return standing;
    }

    public void setStanding(Boolean standing) {
        this.standing = standing;
    }

    public Boolean isTalking() {
        return talking;
    }

    public void setTalking(Boolean talking) {
        this.talking = talking;
    }
}
