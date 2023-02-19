package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.commons.VpaSourceType;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.util.Date;

public class DefaultVpaSource extends VpaSource {

  private final File folder;

  public DefaultVpaSource(@NonNull File folder) {
    this.folder = folder;
  }

  @Override
  public String getType() {
    return VpaSourceType.File.name();
  }

  @Override
  public Date getCreatedAt() {
    return new Date(folder.lastModified());
  }

  @Override
  public Long getId() {
    return -1l;
  }

  @Override
  public String getName() {
    return "Local Repository";
  }

  @Override
  public String getLocation() {
    return folder.getAbsolutePath();
  }
}
