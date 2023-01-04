package de.mephisto.vpin.ui.tables;

import java.util.Arrays;
import java.util.List;

public class POVPostProcComboModel {

  private final static POVPostProcComboModel DEFAULT_MODEL = new POVPostProcComboModel("Default", -1);
  private final static POVPostProcComboModel DISABLED_MODEL = new POVPostProcComboModel("Disabled", 0);
  private final static POVPostProcComboModel ENABLED_MODEL = new POVPostProcComboModel("Fast FXAA", 1);
  private final static POVPostProcComboModel S_FXAA_MODEL = new POVPostProcComboModel("Standard FXAA", 2);
  private final static POVPostProcComboModel Q_NFAA_MODEL = new POVPostProcComboModel("Quality FXAA", 3);
  private final static POVPostProcComboModel F_NFAA_MODEL = new POVPostProcComboModel("Fast NFAA", 4);
  private final static POVPostProcComboModel S_DLAA_MODEL = new POVPostProcComboModel("Standard DLAA", 5);
  private final static POVPostProcComboModel Q_SMAA_MODEL = new POVPostProcComboModel("Quality SMAA", 6);

  public final static List<POVPostProcComboModel> MODELS = Arrays.asList(DEFAULT_MODEL, ENABLED_MODEL, DISABLED_MODEL, S_FXAA_MODEL, Q_NFAA_MODEL, F_NFAA_MODEL, S_DLAA_MODEL, Q_SMAA_MODEL);

  private String label;
  private int value;

  public static POVPostProcComboModel forValue(Object v) {
    if (!(v instanceof Integer)) {
      return DEFAULT_MODEL;
    }

    int value = (int) v;

    if (value == -1) {
      return DEFAULT_MODEL;
    }
    else if (value == 0) {
      return DEFAULT_MODEL;
    }
    else if (value == 1) {
      return ENABLED_MODEL;
    }
    else if (value == 2) {
      return S_FXAA_MODEL;
    }
    else if (value == 3) {
      return Q_NFAA_MODEL;
    }
    else if (value == 4) {
      return F_NFAA_MODEL;
    }
    else if (value == 5) {
      return S_DLAA_MODEL;
    }
    else if (value == 6) {
      return Q_SMAA_MODEL;
    }
    return DISABLED_MODEL;
  }

  public POVPostProcComboModel(String label, int value) {
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
    if (!(obj instanceof POVPostProcComboModel)) {
      return false;
    }
    return label.equals(((POVPostProcComboModel) obj).label);
  }
}
