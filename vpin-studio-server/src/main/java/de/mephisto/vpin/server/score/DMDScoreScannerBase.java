package de.mephisto.vpin.server.score;

import de.mephisto.vpin.server.system.SystemService;

/**
 * A simple Processor that writes frames in a file
 */
public abstract class DMDScoreScannerBase implements DMDScoreProcessor {

  public static String TESSERACT_FOLDER = SystemService.RESOURCES + "tesseract";

 
  // Resize images
  protected int scale = 4;
  // Add border arround to avoid text too closed to borders
  protected int border = 4;
  // Apply a blur effect
  protected int radius = 1;
  

  @Override
  public String onFrameReceived(Frame frame, int[] palette) {
    int W = (frame.getWidth() + 2 * border) * scale;
    int H = (frame.getHeight() + 2 * border) * scale;
    int size = 2 * radius + 1;
    size *= size;

    // Apply the transformations, add an empty border, rescale and recolor, then blur
    byte[] pixels = rescale(frame.getPlane(), frame.getWidth(), frame.getHeight(), border, scale, (byte) size);
    if (radius > 0) {
      pixels = blur(pixels, W, H, radius);
    }
  
    return extractText(frame, pixels, W, H);
  }

  protected abstract String extractText(Frame frame, byte[] pixels, int W, int H);

  //--------------------

  protected byte[] rescale(byte[] plane, int width, int height, int border, int scale, byte black) {
    return crop(plane, width, height, border, 0, width, 0, height, scale, black);
  }

  protected byte[] crop(byte[] plane, int width, int height, int border, 
    int xFrom, int xTo, int yFrom, int yTo, int scale, byte black) {
          
    int newWith = xTo - xFrom + 2 * border;
    int newHeight = yTo - yFrom + 2 * border;
    byte[] scaledPlane = new byte[newWith * scale * newHeight * scale];

    for (int y = yFrom; y < yTo; y++) {
      for (int x = xFrom; x < xTo; x++) {
        for (int dy = 0; dy < scale; dy++) {
          for (int dx = 0; dx < scale; dx++) {
            scaledPlane[ ((y - yFrom + border) * scale + dy) * newWith * scale + (x - xFrom + border) * scale + dx] = (plane[y * width + x] == 0 ? 0: black);
          }
        }
      }
    }
    return scaledPlane;
  }

  protected byte[] blur(byte[] pixels, int width, int height, int radius) {
    int size = radius * 2 + 1;
    size *= size;
    float f = 1.0f / size;

    byte[] outPixels = new byte[pixels.length];
    int index = 0;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
        float color = 0;

        for (int row = -radius; row <= radius; row++) {
					int iy = y+row;
					if (iy < 0 || iy >= height) continue;

					for (int col = -radius; col <= radius; col++) {
            int ix = x+col;
            if (ix < 0 || ix >= width) continue;

            color += f * pixels[iy*width+ix];
					}
				}
				outPixels[index++] = (byte) (color<= size ? color : size);
			}
		}
    return outPixels;
  }
}
