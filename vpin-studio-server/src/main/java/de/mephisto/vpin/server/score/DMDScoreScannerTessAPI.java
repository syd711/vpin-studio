package de.mephisto.vpin.server.score;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;

import net.sourceforge.tess4j.ITessAPI.TessBaseAPI;
import net.sourceforge.tess4j.ITessAPI.TessOcrEngineMode;
import net.sourceforge.tess4j.TessAPI;

/**
 * A simple Processor that writes frames in a file
 */
public class DMDScoreScannerTessAPI extends DMDScoreScannerBase {
  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreScannerTessAPI.class);

  TessAPI api;
  TessBaseAPI handle;

  @Override
  public void onFrameStart(String gameName) {

    File tessDataFolder = new File(TESSERACT_FOLDER, "tessdata");

    api = TessAPI.INSTANCE;
    handle = api.TessBaseAPICreate();
  
    String language = "eng";
    String datapath = tessDataFolder.getPath();
    int psm = TessAPI.TessPageSegMode.PSM_SINGLE_BLOCK;
    int ocrEngineMode = TessOcrEngineMode.OEM_DEFAULT;

    ArrayList<String> configList = new ArrayList<>();
    StringArray sarray = new StringArray(configList.toArray(new String[0]));
    PointerByReference configs = new PointerByReference();
    configs.setPointer(sarray);
    api.TessBaseAPIInit1(handle, datapath, language, ocrEngineMode, configs, configList.size());
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


  @Override
  protected String extractText(Frame frame, byte[] pixels, int W, int H) {

    ByteBuffer buf = ByteBuffer.wrap(pixels);

    try {
/*
      api.TessBaseAPIInit3(handle, datapath, language);
      api.TessBaseAPISetPageSegMode(handle, TessPageSegMode.PSM_AUTO);
      Pointer utf8Text = api.TessBaseAPIRect(handle, buf, bytespp, bytespl, 0, 0, 1024, 800);
      String result = utf8Text.getString(0);
      api.TessDeleteText(utf8Text);
*/
      String content;
  
      int bytespp = 1;
      int bytespl = (int) Math.ceil(W);
      api.TessBaseAPISetImage(handle, buf, W, H, bytespp, bytespl);

      Pointer textPtr = api.TessBaseAPIGetUTF8Text(handle);
      content = textPtr.getString(0);
      api.TessDeleteText(textPtr);

      LOG.info("Text recognized:\n" + content);
      return content;
    } catch (Exception e) {
      LOG.error("Error in OCR :" + e.getMessage());
    }
    return null;
  }
}
