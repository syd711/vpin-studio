package de.mephisto.vpin.restclient.system;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FolderRepresentation extends FileRepresentation {
  private List<FolderRepresentation> children = new ArrayList<>();

  public FolderRepresentation(File file) {
    try {
      this.setName(file.getName().equals("") ? file.getCanonicalPath() : file.getName());
      this.setPath(file.getAbsolutePath());
    }
    catch (IOException e) {
      this.setName(file.getName());
    }
  }

  public FolderRepresentation() {

  }

  public List<FolderRepresentation> getChildren() {
    return children;
  }

  public void setChildren(List<FolderRepresentation> children) {
    this.children = children;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    FolderRepresentation that = (FolderRepresentation) o;
    return Objects.equals(children, that.children);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(children);
  }

  @Override
  public String toString() {
    return getName();
  }
}
