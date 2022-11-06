package org.oskari.example.up;

import org.oskari.example.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScenarioExecutionUP {

    public Integer id;
    public String event;
    public String value;
    public String created_on;
    public Integer scenario_id;

    public ScenarioExecutionUP() {
    }

    @Override
    public String toString() {
        return "{"
                + "scenario_id='" + scenario_id.toString() + "'"
                + ",id=" + id
                + ", event='" + event + "'"
                + ", value='" + value + "'"
                + ", created_on='" + created_on + "'}";
    }
}