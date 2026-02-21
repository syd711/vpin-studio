package de.mephisto.vpin.ui.vpxz.dialogs;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.vpxz.VPXZDescriptorRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.mephisto.vpin.ui.Studio.client;

public class VPXZInstallationProgressModel extends ProgressModel<VPXZDescriptorRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(VPXZInstallationProgressModel.class);

  private final Iterator<VPXZDescriptorRepresentation> iterator;
  private final List<VPXZDescriptorRepresentation> fileName;
  private AtomicBoolean cancelled = new AtomicBoolean(false);

  public VPXZInstallationProgressModel(String title, VPXZDescriptorRepresentation model) {
    super(title);
    this.fileName = Arrays.asList(model);
    this.iterator = fileName.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public VPXZDescriptorRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(VPXZDescriptorRepresentation model) {
    return "Uploading " + model.getFilename();
  }

  @Override
  public int getMax() {
    return fileName.size();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, VPXZDescriptorRepresentation next) {
    client.getVpxzService().install(next);

    waitForInstallation(progressResultModel);
  }

  private void waitForInstallation(ProgressResultModel progressResultModel) {
    if (cancelled.get()) {
      return;
    }
    JFXFuture.supplyAsync(() -> {
      return client.getVpxzService().getProgress();
    }).thenAcceptLater(progress -> {
      if (progress == -1) {
        WidgetFactory.showAlert(Studio.stage, "Installation Failed", "Invalid connection or the phone is in energy saving mode.");
      }
      else {
        progressResultModel.setProgress(progress);
        try {
          Thread.sleep(1000);
        }
        catch (InterruptedException e) {
          //ignore
        }

        if (progress < 1) {
          waitForInstallation(progressResultModel);
        }
      }
    });
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public void cancel() {
    cancelled.set(true);
    client.getVpxzService().cancelInstall();
  }
}
