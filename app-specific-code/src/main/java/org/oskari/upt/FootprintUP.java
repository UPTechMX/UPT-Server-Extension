package org.oskari.upt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FootprintUP {
    public String name;
    public Float value;
    public String location;
    public Integer scenario;
}
