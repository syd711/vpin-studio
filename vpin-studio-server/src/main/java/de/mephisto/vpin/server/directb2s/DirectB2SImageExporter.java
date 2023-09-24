package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.server.VPinStudioException;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DirectB2SImageExporter {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SImageExporter.class);

  private final DirectB2SData data;

  public DirectB2SImageExporter(@NonNull DirectB2SData data) {
    this.data = data;
  }

  public void extractBackground(@NonNull File target) throws VPinStudioException {
    byte[] bytes = DatatypeConverter.parseBase64Binary(data.getBackgroundBase64());
    write(bytes, target);
  }

  public void extractDMD(@NonNull File target) throws VPinStudioException {
    if (data.getDmdBase64() != null) {
      byte[] bytes = DatatypeConverter.parseBase64Binary(data.getDmdBase64());
      write(bytes, target);
    }
  }

  private void write(byte[] bytes, File target) throws VPinStudioException {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(target);
      IOUtils.write(bytes, out);
      out.close();
    } catch (Exception e) {
      String msg = "Failed to write directB2S image '" + data.getName() + "': " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinStudioException(msg, e);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
  }
}
