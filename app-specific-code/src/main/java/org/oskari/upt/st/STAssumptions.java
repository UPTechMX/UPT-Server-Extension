package org.oskari.upt.st;

public class STAssumptions {

    public Integer id;
    public Long study_area;
    public Integer scenario;
    public String category;
    public String name;
    public Double value;

    public STAssumptions(Long study_area) {
        this.study_area=study_area;
    }
}
