package de.mephisto.vpin.ui.vpxz;

import de.mephisto.vpin.restclient.vpxz.VPXZDescriptorRepresentation;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;

public class VPXZModel extends BaseLoadingModel<VPXZDescriptorRepresentation, VPXZModel> {

  public VPXZModel(VPXZDescriptorRepresentation backup) {
    super(backup);
  }

  @Override
  public boolean sameBean(VPXZDescriptorRepresentation object) {
    return object.equals(getBean());
  }

  @Override
  public void load() {

  }

  @Override
  public String getName() {
    return getBean().getFilename();
  }
}
