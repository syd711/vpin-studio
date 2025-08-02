package de.mephisto.vpin.restclient.backups;

public enum AuthenticationProvider {
  VPU,
  VPF;

  public String toString() {
    switch (this) {
      case VPF: {
        return "VP Forum";
      }
      case VPU: {
        return "VP Universe";
      }
    }
    return null;
  }
}
