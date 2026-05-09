package de.mephisto.vpin.restclient.components;

public enum ComponentType {
  //do not change order of declaration, ordinal is used in DB
  vpinmame(1), vpinball(0), b2sbackglass(2), freezy(3), flexdmd(4), serum(5), doflinx(6), dof(7);

  /**
   * The order for display
   */
  private int order;

  ComponentType(int order) {
    this.order = order;
  }

  public int getOrder() {
    return order;
  }

  public static ComponentType[] getValues() {
    return new ComponentType[]{vpinmame, vpinball, b2sbackglass, freezy, flexdmd, doflinx, dof};
  }

  @Override
  public String toString() {
      return switch (this) {
          case vpinmame -> "VPin MAME";
          case vpinball -> "Visual Pinball";
          case b2sbackglass -> "Backglass Server";
          case freezy -> "Freezy";
          case flexdmd -> "FlexDMD";
          case serum -> "Serum";
          case dof -> "DOF";
          case doflinx -> "DOFLinx";
          default -> throw new UnsupportedOperationException("Invalid component type " + this);
      };
  }
}
