package org.oskari.upt.st;

public class STPublicSettings {
  public Long id;
  public String st_layer_id;
  public Integer normalization_method;
  public Double range_min;
  public Double range_max;
  public Integer smaller_better;
  public Double weight;
  public String label;

  public STPublicSettings(String st_layers_id) {
    id = 0L;
    st_layer_id = "pub_" + st_layers_id;
    normalization_method = 3;
    range_min = 0.0;
    range_max = 1.0;
    smaller_better = 0;
    weight = 0D;
    label = "";
  }
}
