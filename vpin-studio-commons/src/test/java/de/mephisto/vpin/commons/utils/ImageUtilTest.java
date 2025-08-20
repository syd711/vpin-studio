package de.mephisto.vpin.commons.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import de.mephisto.vpin.commons.fx.ImageUtil;

public class ImageUtilTest {

  @Test
  public void testClonePerformance() throws Exception {
    try (InputStream in = getClass().getResourceAsStream("../fx/competition-bg-default.png")) {
      BufferedImage avtr = ImageIO.read(in);
      int nb = 1000;
      long time = runNTimes(() -> ImageUtil.clone(avtr), nb);
      assertTrue(time < 1500);
   }
  }

  @Test
  public void testCompareBlur() throws Exception {
    try (InputStream in = getClass().getResourceAsStream("../fx/competition-bg-default.png")) {
      BufferedImage avtr = ImageIO.read(in);
      int nb = 50;
      final int radius = 150;
      //long time0 = runNTimes(() -> ImageUtil.blurImage(avtr, 150), nb);
      long time1 = runNTimes(() -> ImageUtil.fastBlur(avtr, radius), nb);
      long time2 = runNTimes(() -> ImageUtil.boxBlurImage(avtr, radius), nb);
      time2 += runNTimes(() -> ImageUtil.boxBlurImage(avtr, radius), nb);
      time1 += runNTimes(() -> ImageUtil.fastBlur(avtr, radius), nb);

      System.out.println("time1<time2 " + time1 + ", " + time2);
      //assertTrue(time1 < time2);
      assertTrue(time1 < 2500);
   }
  }

  protected long runNTimes(Runnable run, int nbTimes) throws Exception {
    long start = System.currentTimeMillis();
    for (int i = 0 ; i < nbTimes; i++) {
      run.run();
    }
    return System.currentTimeMillis() - start;
  }


}
