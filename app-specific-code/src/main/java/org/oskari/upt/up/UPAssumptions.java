package org.oskari.upt.up;

public class UPAssumptions {
    public Integer id;
    public Long study_area;
    public Integer scenario;
    public String category;
    public String name;
    public Double value;
    public String units;
    public String description;
    public String source;
    public UPAssumptions(Integer scenario){
        this.scenario=scenario;
    }
    
}
