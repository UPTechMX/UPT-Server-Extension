package org.oskari.upt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultsUP {

    private Integer results_id;
    private Integer scenario_id;
    private String name;
    private Float value;
    private String units;
    private String label;

    public ResultsUP() {
        this.units=null;
        this.label=null;
    }

    public Integer getResultsId() {
        return results_id;
    }

    public void setResultsId(Integer results_id) {
        this.results_id = results_id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        value = value;
    }
    public Integer getScenarioId() {
        return scenario_id;
    }

    public void setScenarioId(Integer scenario_id) {
        this.scenario_id = scenario_id;
    }
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }



    @Override
    public String toString() {
        return "results{" +
                "results_id='" + results_id.toString() + "'" +
                ", name='" + name + "'" +
                ", value='" + value.toString() + "'" +
                ", scenario_id='" + scenario_id.toString() +
                '}';
    }
}