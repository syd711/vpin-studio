package de.mephisto.vpin.server.score;

import java.io.File;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;

import net.sourceforge.tess4j.ITessAPI.TessBaseAPI;
import net.sourceforge.tess4j.TessAPI;

/**
 * A simple Processor that writes frames in a file
 */
public class DMDScoreSplitAndScan extends DMDScoreScannerBase {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreSplitAndScan.class);

  TessAPI api;
  TessBaseAPI handle;

  @Override
  public void onFrameStart(String gameName) {

    File tessDataFolder = new File(TESSERACT_FOLDER, "tessdata");

    api = TessAPI.INSTANCE;
    handle = api.TessBaseAPICreate();
  
    String language = "eng";
    String datapath = tessDataFolder.getAbsolutePath();
    int psm = TessAPI.TessPageSegMode.PSM_AUTO;
    //int ocrEngineMode = TessOcrEngineMode.OEM_DEFAULT;

    //ArrayList<String> configList = new ArrayList<>();
    //StringArray sarray = new StringArray(configList.toArray(new String[0]));
    //PointerByReference configs = new PointerByReference();
    //configs.setPointer(sarray);
    //api.TessBaseAPIInit1(handle, datapath, language, ocrEngineMode, configs, configList.size());

    api.TessBaseAPIInit3(handle, datapath, language);

    if (psm > -1) {
        api.TessBaseAPISetPageSegMode(handle, psm);
    }
  }

  @Override
  public void onFrameStop(String gameName) {
    api.TessBaseAPIDelete(handle);
    handle = null;
    api = null;
  }


  protected String extractText(Frame frame, byte[] pixels, int W, int H) {
    return splitV(frame.getPlane(), frame.getWidth(), frame.getHeight(), pixels, W, H);
  }

  private String splitV(byte[] plane, int width, int height, byte[] image, int W, int H) {
    StringBuilder bld = new StringBuilder();

    // first detect full single color vertical lines
    byte previousColor = -1;
    int xStart = 0;
    int xStartSplit = -1;
    for (int x = 0; x < width; x++) {
      byte b = getSingleColumnColor(plane, width, height, x);
      // detection of a full white column
      if (b == 0) {
        // detection of a split
        if (previousColor > 0) {
          String txt = splitH(plane, width, height, xStart, xStartSplit, image, W, H);
          bld.append(txt);
          // separate left from right
          bld.append("\n");
          xStart = x;
        }
        previousColor = 0;
      }
      else if (b > 0 && (previousColor == 0 || previousColor == b)) {
        previousColor = b;
        xStartSplit = x;
      } else {
        previousColor = -1;
      }
    }

    String txt = splitH(plane, width, height, xStart, width, image, W, H);
    bld.append(txt);

    return bld.toString();
  }


  private String splitH(byte[] plane, int width, int height, int xFrom, int xTo, byte[] image, int W, int H) {
    StringBuilder bld = new StringBuilder();

    // first detect full single color horizontal lines
    int yStart = -1;
    for (int y = 0; y < height; y++) {
      int x = getFirstColorX(plane, width, height, xFrom, xTo, y);
      // detection of a full white row
      if (x < 0) {
        // detection of a split
        if (yStart >= 0) {
          String txt = process(plane, width, height, xFrom, xTo, yStart, y, image, W, H);
          bld.append(txt);
          yStart = -1;
        }
      }
      else if (yStart < 0) {
        yStart = y;
      }
    }
    
    if (yStart >= 0) {
      String txt = process(plane, width, height, xFrom, xTo, yStart, height, image, W, H);
      bld.append(txt);
    }

    return bld.toString();
  }

  protected String process(byte[] plane, int width, int height, int _xFrom, int _xTo, int _yFrom, int _yTo, byte[] image, int W, int H) {
    ByteBuffer buf = ByteBuffer.wrap(image);

    String content = null;
  
    int xFrom = _xFrom * scale + border; 
    int xTo = _xTo * scale + border; 
    int yFrom = _yFrom * scale + border; 
    int yTo = _yTo * scale + border; 

    int bytespp = 1;
    int bytespl = (int) Math.ceil(W);

    //Pointer textPtr = api.TessBaseAPIRect(handle, buf, bytespp, bytespl, xFrom - 2, yFrom - 2, xTo - xFrom + 4, yFrom - yTo + 4);

    api.TessBaseAPISetImage(handle, buf, W, H, bytespp, bytespl);
    //api.TessBaseAPISetRectangle(handle,  xFrom - 2, yFrom - 2, xTo - xFrom + 4, yFrom - yTo + 4);
    //Pointer textPtr = api.TessBaseAPIGetUTF8Text(handle);

    Pointer textPtr = api.TessBaseAPIGetUTF8Text(handle);

    if (textPtr != null) {
      content = textPtr.getString(0);
      api.TessDeleteText(textPtr);
    }
    LOG.info("Text recognized:\n" + content);
    return content;
  }

  protected byte getSingleColumnColor(byte[] plane, int width, int height, int x) {
    byte columnColor = -10;
    for (int y = 0; y < height; y++) {
      byte color = plane[y * width + x];
      if (columnColor==-10) {
        columnColor = color;
      }
      else if (color != columnColor) {
        return -1;
      }
    } 
    return columnColor;
  }

  protected int getFirstColorX(byte[] plane, int width, int height, int xFrom, int xTo, int y) {
    for (int x = xFrom; x < xTo; x++) {
      byte color = plane[y * width + x];
      if (color > 0) {
        return x;
      }
    } 
    return -1;
  }
}
