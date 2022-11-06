package org.oskari.upt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MmuUP {

    public List<MmuInfoUP> mmu_info;
    public String mmu_code;
    public Integer scenario;
    public String location;

    public MmuUP() {
        mmu_info=new ArrayList<>();
    }

    public String getMmuCode() {
        return mmu_code;
    }

    public void setMmuCode(String mmuCode) {
        this.mmu_code = mmuCode;
    }

    public List<MmuInfoUP> getMmuInfo() {
        return mmu_info;
    }

    public void setMmuInfo(MmuInfoUP mmuInfo) {
        this.mmu_info.add(mmuInfo);
    }

    public void setScenarioId(Integer scenarioId) {
        this.scenario = scenarioId;
    }

    public Integer setScenarioId() {
        return scenario;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        String values_ = "";
        for (Object mmuInfo : this.mmu_info) {
            values_ += mmu_info.toString();
        }

        values_ += ",";
        values_ = StringUtils.substring(values_, 0, values_.length() - 1);
        return "{"
                + "mmu_ode: '" + mmu_code + "',"
                + "scenario: '" + scenario.toString() + "',"
                + "location: '" + location + "',"
                + "mmu_nfo: '" + values_ + "'"
                + '}';
    }
}
