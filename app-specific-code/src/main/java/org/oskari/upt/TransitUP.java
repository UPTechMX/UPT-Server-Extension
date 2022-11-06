package org.oskari.upt;

import java.util.ArrayList;
import java.util.List;

public class TransitUP {
    public Integer transit_id;
    public Integer scenario;
    public String fclass;
    public String location;
    public String buffer;
    public Long oskari_code;
    public List<TableInfo> transit_info;

    public TransitUP() {
        transit_info=new ArrayList<>();
    }
}