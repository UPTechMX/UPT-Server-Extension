package org.oskari.upt;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import org.apache.commons.lang.StringUtils;

public class ResultsScenarioUP {
    public Integer scenario_id;
    public String name;
    public ResultsValuesUP results[];

    public ResultsScenarioUP() {

    }

    @JsonbCreator
    public ResultsScenarioUP(
      @JsonbProperty("name") String name,
      @JsonbProperty("results") ResultsValuesUP results[]) {

      this.name = name;
      this.results = results;
    }

    @Override
    public String toString() {
        String values_ ="";
        for (int i=0;i<this.results.length;i++){
            values_+=this.results[i].toString();
            values_+=",";
        }
        values_=StringUtils.substring(values_, 0, values_.length() - 1);
        return "{scenario_id:"+this.scenario_id.toString()+",name:'"+this.name+"',results:["+values_+"]}";
    }
}