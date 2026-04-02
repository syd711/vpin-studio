package de.mephisto.vpin.server.nvrams;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.mephisto.vpin.server.nvrams.decoder.NVRamToolDecoder;
import de.mephisto.vpin.server.nvrams.decoder.NVRamToolMapGenerator;
import de.mephisto.vpin.server.nvrams.decoder.NVRamToolDecoder.SearchResult;

/**
 */
public class NVRamToolDecoderTest {

  @Test
  public void testSearch() {
    byte[] data = {
      'H', 'E', 'A', 'D', 'E', 'R',
      'V', 'A', 'L', 'U', 'E', '=',
      (byte) 0x01, (byte) 0x23, (byte) 0x45,  // 12345 in BCD on 3 bytes
      'E', 'N', 'D'
    };

    NVRamToolDecoder decoder = new NVRamToolDecoder();
    SearchResult result = decoder.search(data, "VALUE=", 12345, 4);

    assertNotNull(result);
    assertEquals(6, result.initialPosition);
    assertEquals(12, result.scorePosition);
    assertEquals(3, result.scoreLength);
  }

  @Test
  public void testSearchNumber() {
    byte[] data = {
      'H', 'E', 'A', 'D', 'E', 'R',
      (byte) 0x00, (byte) 0x01, (byte) 0x23, (byte) 0x45,  // 012345 in BCD on 3 bytes or 00012345 on 4 bytes
      'E', 'N', 'D'
    };

    NVRamToolDecoder decoder = new NVRamToolDecoder();
    List<SearchResult> results = decoder.searchNumber(data, 12345, -1, 16, true);

    assertEquals(2, results.size());

    assertEquals(6, results.get(0).scorePosition);
    assertEquals(4, results.get(0).scoreLength);

    assertEquals(7, results.get(1).scorePosition);
    assertEquals(3, results.get(1).scoreLength);
  }

  @Test
  public void testCapitalize() {
    assertEquals(" Martians Destroyed", NVRamToolMapGenerator.capitalize(" MARTIANS DESTROYED"));
  }
}
