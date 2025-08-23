package de.mephisto.vpin.server.assets;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.mephisto.vpin.restclient.assets.AssetMetaData;

public class AssetServiceTest {

  @Test
  public void testMetadata() throws Exception {

    File wheelIcon;
    //wheelIcon = new File("../testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/Wheel/Jaws.png");
    wheelIcon = new File("../testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/Wheel/Jaws (Animated).gif");
    //wheelIcon = new File("./testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/Wheel/Jaws (Animated).apng");
    //wheelIcon = new File("../testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/Wheel/Atlantis (Bally 1989).apng");

    AssetMetaData metadata = AssetService.readVideoAndImageMetadata(wheelIcon);
    assertNotNull(metadata);

    /*
    for (java.util.Map.Entry<String, Object> m : metadata.getData().entrySet()) {
      System.out.println(m.getKey() + " = " + m.getValue());
    }
*/
  }

}
