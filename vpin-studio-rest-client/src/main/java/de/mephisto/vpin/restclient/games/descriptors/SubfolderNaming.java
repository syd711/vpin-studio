package de.mephisto.vpin.restclient.games.descriptors;

public enum SubfolderNaming {
  TABLE_NAME("Table Name"),
  TABLE_DISPLAY_NAME("Table Display Name"),
  TABLE_FILENAME("Table Filename");

  private final String label;

  SubfolderNaming(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public String toString() {
    return label;
  }
}
