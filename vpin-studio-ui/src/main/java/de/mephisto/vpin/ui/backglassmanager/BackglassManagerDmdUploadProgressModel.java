package de.mephisto.vpin.ui.backglassmanager;

import static de.mephisto.vpin.ui.Studio.client;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;

public class BackglassManagerDmdUploadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(BackglassManagerDmdUploadProgressModel.class);

  private DirectB2S directb2S;

  private final Iterator<File> iterator;

  public BackglassManagerDmdUploadProgressModel(String title, DirectB2S directb2S, File file) {
    super(title);
    this.directb2S = directb2S;
    this.iterator = Arrays.asList(file).iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public File getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(File f) {
    return f != null? 
      "Setting DMD image \"" + f.getName() + "\" in backglass" : 
      "Removing DMD image from backglass";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File f) {
    try {
      if (f != null) {
        client.getBackglassServiceClient().uploadDMDImage(directb2S, f);
      } else {
        client.getBackglassServiceClient().removeDMDImage(directb2S);
      }
    } 
    catch (Exception e) {
      LOG.error("Error waiting for server: " + e.getMessage(), e);
    }
  }
}