package org.oskari.upt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class MmuInfoUP {

    public String name;
    public Float value;
    public Integer mmu;

    public MmuInfoUP() {
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
    public Integer getMmu() {
        return this.mmu;
    }

    public void setMmu(Integer mmu) {
        this.mmu = mmu;
    }

    @Override
    public String toString() {
        return "{"
                + "name: '" + name + "',"
                + "value: " + value +
                + '}';
    }
}
