package org.oskari.upt;

import java.util.ArrayList;
import java.util.List;

public class Amenities {
    public Integer amenities_id;
    public Long oskari_code;
    public Integer scenario;
    public String fclass;
    public String location;
    public String buffer;
    public List<TableInfo> amenities_info;

    public Amenities() {
        amenities_info=new ArrayList<>();
    }
}
