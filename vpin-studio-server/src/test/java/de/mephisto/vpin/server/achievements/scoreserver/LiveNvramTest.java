package de.mephisto.vpin.server.achievements.scoreserver;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class LiveNvramTest {

  private static byte[] random(int size, long seed) {
    byte[] data = new byte[size];
    new Random(seed).nextBytes(data);
    return data;
  }

  @Test
  public void alignsSnapshotAtTheStart() {
    byte[] file = random(8192, 1);
    byte[] snapshot = Arrays.copyOf(file, 2048);
    assertThat(LiveNvram.align(file, snapshot)).isEqualTo(0);
  }

  @Test
  public void alignsSnapshotInsideTheFile() {
    byte[] file = random(8192, 2);
    byte[] snapshot = Arrays.copyOfRange(file, 512, 512 + 2048);
    // the live values differ from the file in some places (game in progress)
    snapshot[10] ^= 0x55;
    snapshot[700] ^= 0x55;
    assertThat(LiveNvram.align(file, snapshot)).isEqualTo(512);
  }

  @Test
  public void blankSnapshotIsNotAligned() {
    byte[] file = random(4096, 3);
    assertThat(LiveNvram.align(file, new byte[1024])).isNull();
    assertThat(LiveNvram.align(file, random(1024, 99))).isNull();
    assertThat(LiveNvram.align(new byte[0], random(16, 4))).isNull();
  }

  @Test
  public void mergesSnapshotIntoCopyOfTheFile() {
    byte[] file = random(100, 5);
    byte[] original = file.clone();
    byte[] snapshot = new byte[30];
    Arrays.fill(snapshot, (byte) 7);
    byte[] merged = LiveNvram.merge(file, 80, snapshot);

    assertThat(file).isEqualTo(original);
    assertThat(merged).hasSize(100);
    assertThat(Arrays.copyOfRange(merged, 0, 80)).isEqualTo(Arrays.copyOfRange(original, 0, 80));
    // the part of the snapshot behind the end of the file is cut off
    assertThat(Arrays.copyOfRange(merged, 80, 100)).containsOnly((byte) 7);
  }
}
