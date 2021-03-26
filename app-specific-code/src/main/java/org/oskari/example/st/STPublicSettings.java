package org.oskari.example.st;

public class STPublicSettings {
  public Long id;
  public Long st_public_layer_id;
  public Integer normalization_method;
  public Double range_min;
  public Double range_max;
  public Integer smaller_better;
  public Double weight;
  public String label;

  public STPublicSettings(Long st_public_layers_id) {
    id = 0L;
    st_public_layer_id = st_public_layers_id;
    normalization_method = 3;
    range_min = 0.0;
    range_max = 1.0;
    smaller_better = 0;
    weight = 0D;
    label = "";
  }
}
