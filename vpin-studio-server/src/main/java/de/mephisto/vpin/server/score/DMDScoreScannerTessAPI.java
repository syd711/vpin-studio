package de.mephisto.vpin.server.score;

import java.io.File;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;

import de.mephisto.vpin.server.system.SystemService;
import net.sourceforge.tess4j.ITessAPI.TessBaseAPI;
import net.sourceforge.tess4j.ITessAPI.TessOcrEngineMode;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.TessAPI;

/**
 * A simple Processor that writes frames in a file
 */
public class DMDScoreScannerTessAPI extends DMDScoreProcessorBase {
  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreScannerTessAPI.class);

  public static String TESSERACT_FOLDER = SystemService.RESOURCES + "tessdata";

  /** The max height of images that are considered as Low Resolution */
  protected int THRESHOLD_LOWRES = 6;
  /** Rescale factor for low Resolution images */
  protected int SCALE_LOWRES = 3;
  /** Rescale factor for non low res images */
  protected int SCALE_HIGHRES = 4;

  // Add border arround to avoid text too closed to borders
  protected int BORDER = 2;
  // Apply a blur effect
  protected int RADIUS = 1;

  private TessAPI api;
  private TessBaseAPI handle;

  /** Whether image is one line or a blpock */
  private boolean singleLine = true;
  /** disconnect usage of dictionaries */
  boolean useDictionary = false;

  public DMDScoreScannerTessAPI(boolean singleLine) {
    this.singleLine = singleLine;
  }

  @Override
  public void onFrameStart(String gameName) {
    super.onFrameStart(gameName);

    File tessDataFolder = new File(TESSERACT_FOLDER);

    api = TessAPI.INSTANCE;
    handle = api.TessBaseAPICreate();
  
    String language = "eng";
    String datapath = tessDataFolder.getPath();
    int oem = TessOcrEngineMode.OEM_LSTM_ONLY;

    PointerByReference configs = null;
    int configs_size = 0;
    
    if (useDictionary) {
      api.TessBaseAPIInit1(handle, datapath, language, oem, configs, configs_size);
    }
    else {
      // disable loading dictionaries
      String[] args = new String[] { "load_system_dawg", "load_freq_dawg" };
      StringArray sarray = new StringArray(args);
      PointerByReference vars_vec = new PointerByReference();
      vars_vec.setPointer(sarray);

      args = new String[] { "F", "F" };
      sarray = new StringArray(args);
      PointerByReference vars_values = new PointerByReference();
      vars_values.setPointer(sarray);

      NativeSize vars_vec_size = new NativeSize(args.length);

      api.TessBaseAPIInit4(handle, datapath, language, oem, configs, configs_size, vars_vec, vars_values, vars_vec_size, ITessAPI.FALSE);
    }
    
    if (singleLine) {
      api.TessBaseAPISetPageSegMode(handle, TessAPI.TessPageSegMode.PSM_SINGLE_LINE);
    }
    else {
      api.TessBaseAPISetPageSegMode(handle, TessAPI.TessPageSegMode.PSM_SINGLE_BLOCK);
    }
  }

  @Override
  public void onFrameStop(String gameName) {
    if (api != null) {
      api.TessBaseAPIDelete(handle);
    }
    handle = null;
    api = null;

    super.onFrameStop(gameName);
  }

  @Override
  public String onFrameReceived(Frame frame) {
    // extract text from full frame
    return extractText(frame, 0, frame.getWidth(), 0, frame.getHeight());
  }

  protected String extractText(Frame frame, int xF, int xT, int yF, int yT) {

    int SCALE =  (yT - yF) <= THRESHOLD_LOWRES && (xT - xF) > 8 * (yT - yF) ? SCALE_LOWRES : SCALE_HIGHRES;

    int newW = (xT - xF + 2 * BORDER) * SCALE;
    int newH = (yT - yF + 2 * BORDER) * SCALE;
    int size = 2 * RADIUS + 1;
    size *= size;
    byte blank = getBlankIndex(frame.getPalette());

    // Apply the transformations, add an empty border, rescale and recolor, then blur
    byte[] pixels = crop(frame.getPlane(), frame.getWidth(), frame.getHeight(), xF, xT, yF, yT, BORDER, SCALE, blank, (byte) size);
    pixels = blur(pixels, newW, newH, RADIUS);

    if (DEV_MODE) {
      String imgName = (xF == 0 && yF == 0) ? "" : "_" + xF + "x" + yF;
      File imgFile = new File(folder, 
        StringUtils.defaultIfBlank(frame.getName(), Integer.toString(frame.getTimeStamp())) + "_" + imgName + ".png");
      saveImage(pixels, newW, newH, generateBlurPalette(), imgFile);
    }

    try {
      ByteBuffer buf = ByteBuffer.wrap(pixels);
      api.TessBaseAPISetImage(handle, buf, newW, newH, 1, newW);

      Pointer textPtr = api.TessBaseAPIGetUTF8Text(handle);

      if (textPtr != null) {
        String content = StringUtils.trim(textPtr.getString(0));
        LOG.info("Text recognized:\n" + content);

        api.TessDeleteText(textPtr);

        content  = postProcess(content.toUpperCase());
        LOG.info("Text post processed:\n" + content);

        return content;
      }
    } 
    catch (Exception e) {
      LOG.error("Error in OCR :" + e.getMessage());
    }
    return null;
  }

  /**
   * Apply some text transformation, post processing
   */
  protected String postProcess(String content) {
    content = StringUtils.replace(content, "BALL I", "BALL 1");
    content = StringUtils.replace(content, "BALL >", "BALL 3");

    content = StringUtils.replace(content, "CREDIT? ", "CREDITS ");
    content = StringUtils.replace(content, "CREDITS O", "CREDITS 0");
    content = StringUtils.replace(content, "CREDITS I", "CREDITS 1");

    content = StringUtils.replace(content, "HIGH CORE", "HIGH SCORE");
    content = StringUtils.replace(content, "HIGH SCORE +", "HIGH SCORE #");

    content = StringUtils.replace(content, "OO", "00");

    // else
    return content;
  }
}
