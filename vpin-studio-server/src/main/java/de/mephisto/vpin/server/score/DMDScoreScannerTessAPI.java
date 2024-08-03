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

import net.sourceforge.tess4j.ITessAPI.TessBaseAPI;
import net.sourceforge.tess4j.ITessAPI.TessOcrEngineMode;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.TessAPI;

/**
 * A simple Processor that writes frames in a file
 */
public class DMDScoreScannerTessAPI extends DMDScoreScannerBase {
  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreScannerTessAPI.class);

  private TessAPI api;
  private TessBaseAPI handle;
  
  private boolean singleLine = true;;


  public DMDScoreScannerTessAPI(boolean singleLine) {
    this.singleLine = singleLine;
  }

  @Override
  public void onFrameStart(String gameName) {

    File tessDataFolder = new File(TESSERACT_FOLDER);

    api = TessAPI.INSTANCE;
    handle = api.TessBaseAPICreate();
  
    String language = "eng";
    String datapath = tessDataFolder.getPath();
    int oem = TessOcrEngineMode.OEM_LSTM_ONLY;
    boolean useDictionary = true;

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
  }


  @Override
  protected String extractText(Frame frame, byte[] pixels, int width, int height) {
    try {
      ByteBuffer buf = ByteBuffer.wrap(pixels);
      api.TessBaseAPISetImage(handle, buf, width, height, 1, width);

      Pointer textPtr = api.TessBaseAPIGetUTF8Text(handle);

      if (textPtr != null) {
        String content = StringUtils.trim(textPtr.getString(0));

        api.TessDeleteText(textPtr);

        LOG.info("Text recognized:\n" + content);
        return content;
      }
    } 
    catch (Exception e) {
      LOG.error("Error in OCR :" + e.getMessage());
    }
    return null;
  }
}
