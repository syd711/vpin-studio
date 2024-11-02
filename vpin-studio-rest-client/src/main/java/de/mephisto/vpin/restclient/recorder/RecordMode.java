package de.mephisto.vpin.restclient.recorder;

public enum RecordMode {
  ifMissing, overwrite;


  @Override
  public String toString() {
    if (this.equals(ifMissing)) {
      return "If Missing";
    }
    else if(this.equals(overwrite)) {
      return "Overwrite";
    }

    return this.name();
  }
}
