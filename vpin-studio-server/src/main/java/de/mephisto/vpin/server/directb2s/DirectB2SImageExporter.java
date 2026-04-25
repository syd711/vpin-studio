package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.server.VPinStudioException;
import org.jspecify.annotations.NonNull;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;

public class DirectB2SImageExporter {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SImageExporter.class);

  private final DirectB2SDataExtractor data;

  public DirectB2SImageExporter(@NonNull DirectB2SDataExtractor data) {
    this.data = data;
  }

  public void extractBackground(@NonNull File target) throws VPinStudioException {
    export(target, data.getBackgroundBase64());
  }

  public void extractDMD(@NonNull File target) throws VPinStudioException {
    export(target, data.getDmdBase64());
  }

  public static void export(@NonNull File target, String base64) throws VPinStudioException {
    if (base64 != null) {
      byte[] bytes = Base64.getDecoder().decode(base64);
      write(bytes, target);
    }
  }

  private static void write(byte[] bytes, File target) throws VPinStudioException {
    if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
      LOG.error("Failed to created export directb2s export folder \"{}\"", target.getParentFile().getAbsolutePath());
      return;
    }
    try (FileOutputStream out = new FileOutputStream(target)) {
      IOUtils.write(bytes, out);
      out.close();
      LOG.info("Written backglass export image \"{}\"", target.getAbsolutePath());
    } 
    catch (Exception e) {
      String msg = "Failed to write directB2S image : " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinStudioException(msg, e);
    } 
  }
}
