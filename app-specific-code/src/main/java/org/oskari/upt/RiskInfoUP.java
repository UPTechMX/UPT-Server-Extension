package org.oskari.upt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RiskInfoUP {

    public String name;
    public Float value;
    public Integer risk;

    public RiskInfoUP() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Float getValue() {
        return this.value;
    }

    public void setValue(Float value) {
        this.value = value;
    }
    public Integer getRisk() {
        return this.risk;
    }

    public void setRisk(Integer mmu) {
        this.risk = risk;
    }

    @Override
    public String toString() {
        return "{"
                + "name: '" + name + "',"
                + "value: " + value +
                + '}';
    }
}
