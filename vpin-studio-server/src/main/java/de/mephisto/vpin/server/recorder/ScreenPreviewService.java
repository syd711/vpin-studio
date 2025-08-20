package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.commons.fx.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

@Service
public class ScreenPreviewService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(ScreenPreviewService.class);

  public void capture(@NonNull OutputStream out, @NonNull FrontendPlayerDisplay display) {
    try {
      Rectangle rectangle = new Rectangle(display.getX(), display.getY(), display.getWidth(), display.getHeight());
      Robot robot = new Robot();
      BufferedImage bufferedImage = robot.createScreenCapture(rectangle);
//      ImageUtil.write(bufferedImage, new File("c:/temp/out.jpg"));
      ImageUtil.writeJPG(bufferedImage, out);
    }
    catch (Exception e) {
      LOG.error("Failed to generated screen capture for " + display + ": {}", e.getMessage(), e);
    }
  }

  public void capture(@NonNull OutputStream out, @NonNull MonitorInfo display) {
    try {
      Rectangle rectangle = new Rectangle((int) display.getX(), (int) display.getY(), display.getWidth(), display.getHeight());
      Robot robot = new Robot();
      BufferedImage bufferedImage = robot.createScreenCapture(rectangle);
      ImageUtil.writeJPG(bufferedImage, out);
    }
    catch (Exception e) {
      LOG.error("Failed to generated screen capture for monitor #" + display + ": {}", e.getMessage(), e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {

  }
}
