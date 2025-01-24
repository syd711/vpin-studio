package de.mephisto.vpin.server.frontend.pinballx;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;

@Service
public class PinballXFtpClient {

  private final static Logger LOG = LoggerFactory.getLogger(PinballXFtpClient.class);

  @Value("${pinballX.mediaserver.host}")
  protected String host;
  @Value("${pinballX.mediaserver.port}")
  protected int port;
  @Value("${pinballX.mediaserver.rootfolder}")
  protected String rootfolder;
  @Value("${pinballX.mediaserver.usePassiveMode:true}")
  protected boolean usePassiveMode;

  protected String user;
  protected String pwd;


  public void configureCredentials(String user, String pwd) {
    this.user = user;
    this.pwd = pwd;
  }

  public void configure(String host, int port, String rootfolder) {
    this.host = host;
    this.port = port;
    this.rootfolder = rootfolder;
  }

  public boolean testConnection() {
    FTPClient ftp = null;
    try {
      ftp = open();
      return true;
    }
    catch (Exception e) {
      LOG.error("Cannot log in", e);
      return false;
    }
    finally {
      close(ftp);
    }
  }

  protected FTPClient open() throws IOException {
    return open(usePassiveMode);
  }

  private FTPClient open(boolean passiveMode) throws IOException {
    FTPClient ftp = new FTPClient();
    ftp.connect(host, port);
    ftp.setControlKeepAliveTimeout(Duration.ofMinutes(5));
    ftp.setBufferSize(1024 * 1024);
    if (passiveMode) {
      ftp.enterLocalPassiveMode();
    }
    else {
      ftp.enterLocalActiveMode();
    }
    int reply = ftp.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply)) {
      ftp.disconnect();
      throw new IOException("Exception in connecting to FTP Server");
    }

    if (ftp.login(user, pwd)) {
      return ftp;
    }

    // we tried in passive mode, try again in active mode
    if (passiveMode) {
      return open(false);
    }
    throw new IOException("Error, user cannot log in.");
  }

  protected void close(FTPClient ftp) {
    if (ftp != null) {
      try {
        ftp.disconnect();
      }
      catch (IOException ioe) {
        LOG.error("Error while closing FTP connection", ioe);
      }
    }
  }

}
