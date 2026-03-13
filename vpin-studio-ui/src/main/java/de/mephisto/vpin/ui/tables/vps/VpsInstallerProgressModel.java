package de.mephisto.vpin.ui.tables.vps;

import static de.mephisto.vpin.ui.Studio.client;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.restclient.vps.VpsInstallLink;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;

public class VpsInstallerProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private String link;

  private boolean hasNext = true;

  public VpsInstallerProgressModel(String link) {
    super("Getting files...");
    this.link = link;
  }

  @Override
  public boolean isIndeterminate() {
    return true;
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
    return hasNext;
  }

  @Override
  public String getNext() {
    return link;
  }

  @Override
  public String nextToString(String next) {
    return next;
  }

  public void processNext(ProgressResultModel progressResultModel, String link) {
    try {
      List<VpsInstallLink> links = client.getVpsService().getInstallLinks(link);
      for (VpsInstallLink l : links) {
        progressResultModel.addProcessed(l);
      }
      hasNext = false;
    }
    catch (Exception e) {
      LOG.error("Cannot get links: " + e.getMessage(), e);
    }
  }}
