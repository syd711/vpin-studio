package de.mephisto.vpin.restclient.recorder;

public enum RecordMode {
  ifMissing, overwrite, append;


  @Override
  public String toString() {
    if (this.equals(ifMissing)) {
      return "If Missing";
    }
    else if(this.equals(overwrite)) {
      return "Overwrite";
    }
    else if(this.equals(append)) {
      return "Append";
    }

    return this.name();
  }
}
