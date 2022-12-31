package de.mephisto.vpin.ui.tables;

import java.util.Arrays;
import java.util.List;

public class POVComboModel {

  private final static POVComboModel DEFAULT_MODEL = new POVComboModel("Default", -1);
  private final static POVComboModel ENABLED_MODEL = new POVComboModel("Enabled", 1);
  private final static POVComboModel DISABLED_MODEL = new POVComboModel("Disabled", 0);

  public final static List<POVComboModel> MODELS = Arrays.asList(DEFAULT_MODEL, ENABLED_MODEL, DISABLED_MODEL);

  private String label;
  private int value;

  public static POVComboModel forValue(int value) {
    if (value == -1) {
      return DEFAULT_MODEL;
    }
    else if (value == 1) {
      return ENABLED_MODEL;
    }
    return DISABLED_MODEL;
  }

  public POVComboModel(String label, int value) {
    this.label = label;
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  @Override
  public String toString() {
    return this.label;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof POVComboModel)) {
      return false;
    }
    return label.equals(((POVComboModel) obj).label);
  }
}
