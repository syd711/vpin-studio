package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentInstallation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class ComponentChecksProgressModel extends ProgressModel<ComponentInstallation> {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentChecksProgressModel.class);

  private final Iterator<ComponentInstallation> iterator;
  private final boolean forceDownload;

  public ComponentChecksProgressModel(boolean forceDownload) {
    super("Fetching Latest Github Releases");
    this.forceDownload = forceDownload;
    ArrayList<ComponentInstallation> installs = new ArrayList<>();
    for (ComponentType type : ComponentType.getValues()) {
      ComponentInstallation install = new ComponentInstallation();
      install.setComponent(type);
      install.setReleaseTag("-latest-");
      install.setArtifactName("-latest-");
      install.setTargetFolder("-any-");
      installs.add(install);
    }
    this.iterator = installs.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public ComponentInstallation getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return false;
  }

  @Override
  public String nextToString(ComponentInstallation c) {
    return "Checking \"" + c.getComponent() + "\"";
  }

  @Override
  public int getMax() {
    return ComponentType.getValues().length;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, ComponentInstallation next) {
    try {
      if (progressResultModel.isCancelled()) {
        return;
      }

      ComponentActionLogRepresentation check = client.getComponentService().check(next, forceDownload);
      progressResultModel.getResults().add(check.getStatus());
      if (!StringUtils.isEmpty(check.getStatus())) {
        LOG.error("Failed to check component " + next + ": " + check.getStatus());
      }

      EventManager.getInstance().notify3rdPartyVersionUpdate(next.getComponent());
    }
    catch (Exception e) {
      progressResultModel.getResults().add(e.getMessage());
      LOG.error("Failed to fetch component data: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    List<String> collect = progressResultModel.getResults().stream().filter(Objects::nonNull).map(r -> r + "\n").collect(Collectors.toList());
    if (!collect.isEmpty()) {
      LOG.error("Failed to retrieve some component information: {}", String.join("\n", collect));
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Component Check Failed", "The component update check failed for one or more components.");
      });
    }
  }
}
