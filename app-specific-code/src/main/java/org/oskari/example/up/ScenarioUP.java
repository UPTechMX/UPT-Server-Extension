package org.oskari.example.up;

import org.oskari.example.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScenarioUP {

    public Integer scenario_id;
    public String name;
    public String description;
    public Integer owner_id;
    public Integer study_area;
    public Integer is_base;
    public Integer has_assumptions;

    public ScenarioUP() {
    }

    public Integer getScenarioId() {
        return scenario_id;
    }

    public void setScenarioId(Integer scenario_id) {
        this.scenario_id = scenario_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOwneId() {
        return owner_id;
    }

    public void setOwnerId(Integer owner_id) {
        this.owner_id = owner_id;
    }

    public Integer getIsBase() {
        return is_base;
    }

    public void setIsBase(Integer is_base) {
        this.is_base = is_base;
    }

    public void setStudyArea(Integer study_area) {
        this.study_area = study_area;
    }

    public Integer getStudyArea() {
        return study_area;
    }
}