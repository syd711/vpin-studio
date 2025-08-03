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

  public String getUrl() {
    switch (this) {
      case VPF: {
        return "vpforums.org";
      }
      case VPU: {
        return "vpuniverse.com";
      }
    }
    return null;
  }
}
