package de.mephisto.vpin.server.emulators;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmulatorVersionTest {

  @Test
  void isVpxPerTableFileLayoutSupported_fromVpx1081() {
    assertThat(EmulatorVersion.isVpxPerTableFileLayoutSupported("10.8.1.3788")).isTrue();
    assertThat(EmulatorVersion.isVpxPerTableFileLayoutSupported("10.8.1")).isTrue();
    assertThat(EmulatorVersion.isVpxPerTableFileLayoutSupported("10.8.2.0")).isTrue();
    assertThat(EmulatorVersion.isVpxPerTableFileLayoutSupported("10.9.0.0")).isTrue();
    assertThat(EmulatorVersion.isVpxPerTableFileLayoutSupported("11.0.0.0")).isTrue();
  }

  @Test
  void isVpxPerTableFileLayoutSupported_notForOlderVersions() {
    assertThat(EmulatorVersion.isVpxPerTableFileLayoutSupported("10.8.0.2058")).isFalse();
    assertThat(EmulatorVersion.isVpxPerTableFileLayoutSupported("10.8")).isFalse();
    assertThat(EmulatorVersion.isVpxPerTableFileLayoutSupported("10.7.4.0")).isFalse();
    assertThat(EmulatorVersion.isVpxPerTableFileLayoutSupported("9.9.1.0")).isFalse();
  }

  @Test
  void isVpxPerTableFileLayoutSupported_unknownVersions() {
    assertThat(EmulatorVersion.isVpxPerTableFileLayoutSupported(null)).isFalse();
    assertThat(EmulatorVersion.isVpxPerTableFileLayoutSupported("")).isFalse();
    assertThat(EmulatorVersion.isVpxPerTableFileLayoutSupported("abc")).isFalse();
  }
}
