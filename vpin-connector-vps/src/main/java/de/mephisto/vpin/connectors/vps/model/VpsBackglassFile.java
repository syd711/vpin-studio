package de.mephisto.vpin.connectors.vps.model;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

public class VpsBackglassFile extends VpsAuthoredUrls {

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VpsBackglassFile)) return false;

    VpsBackglassFile that = (VpsBackglassFile) o;

    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return getId() != null ? getId().hashCode() : 0;
  }
}
