package de.mephisto.vpin.restclient.vpauthenticators;

public enum AuthenticationProvider {
  VPU,
  VPF;

  public String toString() {
      return switch (this) {
          case VPF -> "VP Forum";
          case VPU -> "VP Universe";
      };
  }

  public String getUrl() {
      return switch (this) {
          case VPF -> "vpforums.org";
          case VPU -> "vpuniverse.com";
      };
  }
}
