package de.mephisto.vpin.server.directb2s;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.mephisto.vpin.restclient.directb2s.DirectB2SFrameType;
import de.mephisto.vpin.commons.fx.ImageUtil;

/**
 * The frame generator idea comes from Himura95
 * also the implementation in python for ambilight, blurred and mirror effects
 */
public class DirectB2SFrameTypeGenerator {


  public static byte[] generateAsByte(BufferedImage b2s, int screenW, int screenH, DirectB2SFrameType frameType, boolean addOriginalImage) throws IOException {
    BufferedImage img = generate(b2s, screenW, screenH, frameType, addOriginalImage);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "PNG", baos);
    return baos.toByteArray();
  }


  public static BufferedImage generate(BufferedImage b2s, int screenW, int screenH, DirectB2SFrameType frameType, boolean addOriginalImage) {
    double targetW = b2s.getWidth();
    double targetH = b2s.getHeight();
    // has vertical bands ?
    if (targetW * screenH < targetH * screenW) {
      targetW = targetH * screenW / screenH;
    }
    else {
      targetH = targetW * screenH / screenW;
    }

    switch (frameType) {
      case AMBILIGHT:
        return createAmbilightFrame(b2s, (int) targetW, (int) targetH, addOriginalImage);
      case BLURRED:
        return createBlurredFrame(b2s, (int) targetW, (int) targetH, addOriginalImage);
      case MIRROR:
        return createMirrorFrame(b2s, (int) targetW, (int) targetH, addOriginalImage);
      case GRADIENT:
        return createGradientFrame(b2s, (int) targetW, (int) targetH, addOriginalImage);
      default:
        return null;
    }
  }

  //--------------------------------

  public static BufferedImage createAmbilightFrame(BufferedImage img, int targetW, int targetH, boolean addOriginalImage) {
    int w = img.getWidth();
    int h = img.getHeight();
    int padW = (targetW - w) / 2;
    int padH = (targetH - h) / 2;

    BufferedImage ambilightBg = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = ambilightBg.createGraphics();

    if (padW > 0) {
      BufferedImage leftStrip = img.getSubimage(0, 0, 4, h);
      BufferedImage rightStrip = img.getSubimage(w - 4, 0, 4, h);

      BufferedImage leftResized = ImageUtil.resizeImage(leftStrip, padW, h);
      BufferedImage rightResized = ImageUtil.resizeImage(rightStrip, padW, h);

      int radius = Math.max(h / 25, 40);
      BufferedImage leftBlur = ImageUtil.blurImage(leftResized, radius);
      BufferedImage rightBlur = ImageUtil.blurImage(rightResized, radius);

      g.drawImage(leftBlur, 0, 0, null);
      g.drawImage(rightBlur, padW + w, 0, null);
    }
    else if (padH > 0) {
      BufferedImage topStrip = img.getSubimage(0, 0, w, 4);
      BufferedImage bottomStrip = img.getSubimage(0, h - 4, w, 4);

      BufferedImage topResized = ImageUtil.resizeImage(topStrip, w, padH);
      BufferedImage bottomResized = ImageUtil.resizeImage(bottomStrip, w, padH);

      int radius = Math.max(w / 25, 40);
      BufferedImage topBlur = ImageUtil.blurImage(topResized, radius);
      BufferedImage bottomBlur = ImageUtil.blurImage(bottomResized, radius);

      g.drawImage(topBlur, 0, 0, null);
      g.drawImage(bottomBlur, 0, padH + h, null);
    }

    if (addOriginalImage) {
      g.drawImage(img, padW, padH, null);
    }
    g.dispose();
    return ambilightBg;
  }

  public static BufferedImage createBlurredFrame(BufferedImage img, int targetW, int targetH, boolean addOriginalImage) {
    int w = img.getWidth();
    int h = img.getHeight();
    int padW = (targetW - w) / 2;
    int padH = (targetH - h) / 2;

    BufferedImage zoomBg = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = zoomBg.createGraphics();

    if (padW > 0) {
      BufferedImage leftCrop = img.getSubimage(0, 0, padW, h);
      BufferedImage rightCrop = img.getSubimage(w - padW, 0, padW, h);

      BufferedImage leftResized = ImageUtil.resizeImage(leftCrop, padW, h);
      BufferedImage rightResized = ImageUtil.resizeImage(rightCrop, padW, h);

      int radius = Math.max(h / 25, 30);
      BufferedImage leftBlur = ImageUtil.blurImage(leftResized, radius);
      BufferedImage rightBlur = ImageUtil.blurImage(rightResized, radius);

      g.drawImage(leftBlur, 0, 0, null);
      g.drawImage(rightBlur, padW + w, 0, null);
    }
    else if (padH > 0) {
      BufferedImage topCrop = img.getSubimage(0, 0, w, padH);
      BufferedImage bottomCrop = img.getSubimage(0, h - padH, w, padH);

      BufferedImage topResized = ImageUtil.resizeImage(topCrop, w, padH);
      BufferedImage bottomResized = ImageUtil.resizeImage(bottomCrop, w, padH);

      int radius = Math.max(w / 25, 30);
      BufferedImage topBlur = ImageUtil.blurImage(topResized, radius);
      BufferedImage bottomBlur = ImageUtil.blurImage(bottomResized, radius);

      g.drawImage(topBlur, 0, 0, null);
      g.drawImage(bottomBlur, 0, padH + h, null);
    }

    if (addOriginalImage) {
      g.drawImage(img, padW, padH, null);
    }
    g.dispose();
    return zoomBg;
  }

  public static BufferedImage createMirrorFrame(BufferedImage img, int targetW, int targetH, boolean addOriginalImage) {
    int w = img.getWidth();
    int h = img.getHeight();
    int padW = (targetW - w) / 2;
    int padH = (targetH - h) / 2;
    
    BufferedImage refletBg = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = refletBg.createGraphics();

    if (padW > 0) {
      BufferedImage left = img.getSubimage(0, 0, padW, h);
      left = ImageUtil.flipHorizontal(left);
      BufferedImage right = img.getSubimage(w - padW, 0, padW, h);
      right = ImageUtil.flipHorizontal(right);

      BufferedImage leftPersp = ImageUtil.applyPerspective(left, "right", 0.11, 0.2);
      BufferedImage rightPersp = ImageUtil.applyPerspective(right, "left", 0.11, 0.2);

      int radius = 50;
      BufferedImage leftPerspBlur = ImageUtil.blurImage(leftPersp, radius);
      BufferedImage rightPerspBlur = ImageUtil.blurImage(rightPersp, radius);

      g.drawImage(leftPerspBlur, 0, 0, null);
      g.drawImage(rightPerspBlur, padW + w, 0, null);
    }
    else if (padH > 0) {
      BufferedImage top = img.getSubimage(0, 0, w, padH);
      top = ImageUtil.flipVertical(top);
      BufferedImage bottom = img.getSubimage(0, h - padH, w, padH);
      bottom = ImageUtil.flipVertical(bottom);

      BufferedImage topPersp = ImageUtil.applyPerspective(top, "top", 0.2, 0.11);
      BufferedImage bottomPersp = ImageUtil.applyPerspective(bottom, "left", 0.2, 0.11);

      int radius = 50;
      BufferedImage topPerspBlur = ImageUtil.blurImage(topPersp, radius);
      BufferedImage bottomPerspBlur = ImageUtil.blurImage(bottomPersp, radius);

      g.drawImage(topPerspBlur, 0, 0, null);
      g.drawImage(bottomPerspBlur, 0, padH + h, null);
    }
    if (addOriginalImage) {
      g.drawImage(img, padW, padH, null);
    }
    g.dispose();
    return refletBg;
  }

  private static BufferedImage createGradientFrame(BufferedImage img, int targetW, int targetH, boolean addOriginalImage) {

    int w = img.getWidth();
    int h = img.getHeight();
    int padW = (targetW - w) / 2;
    int padH = (targetH - h) / 2;
    
    int[] rgb = ImageUtil.getDominantColor(img, 5);
    Color color = new Color(rgb[0], rgb[1], rgb[2]);

    BufferedImage dest = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = dest.createGraphics();

    if (padW > 0) {
      // Horizontal gradients
      GradientPaint gradient = new GradientPaint(0, 0, color, padW, 0, Color.BLACK);
      g.setPaint(gradient);
      g.fillRect(0, 0, padW, h);

      gradient = new GradientPaint(padW + w, 0, Color.BLACK, targetW, 0, color);
      g.setPaint(gradient);
      g.fillRect(padW + w, 0, padW, h);
    }
    else if (padH > 0) {
      // Vertical gradients
      GradientPaint gradient = new GradientPaint(0, 0, color, 0, padH, Color.BLACK);
      g.setPaint(gradient);
      g.fillRect(0, 0, w, padH);

      gradient = new GradientPaint(0, padH + h, Color.BLACK, 0, targetH, color);
      g.setPaint(gradient);
      g.fillRect(0, padH + h, w, padH);
    }

    if (addOriginalImage) {
      g.drawImage(img, padW, padH, null);
    }
    g.dispose();

    return dest;
  }

}
