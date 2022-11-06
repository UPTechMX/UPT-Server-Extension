package org.oskari.example.up;

import org.oskari.example.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UPFootprint {

    public String name;
    public Float value;
    public String location;
    public Integer scenario;
}
