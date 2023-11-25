package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class ComponentChecksProgressModel extends ProgressModel<ComponentType> {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentChecksProgressModel.class);

  private final Iterator<ComponentType> iterator;
  private final boolean forceDownload;

  public ComponentChecksProgressModel(boolean forceDownload) {
    super("Fetching Latest Github Releases");
    this.forceDownload = forceDownload;
    this.iterator = Arrays.asList(ComponentType.values()).iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public ComponentType getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return false;
  }

  @Override
  public String nextToString(ComponentType c) {
    return "Checking \"" + c.toString() + "\"";
  }

  @Override
  public int getMax() {
    return ComponentType.values().length;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, ComponentType next) {
    try {
      if (progressResultModel.isCancelled()) {
        return;
      }

      ComponentActionLogRepresentation check = client.getComponentService().check(next, "-latest-", forceDownload);
      progressResultModel.getResults().add(check.getStatus());
      EventManager.getInstance().notify3rdPartyVersionUpdate(next);
    } catch (Exception e) {
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
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Component Check Failed", "Failed retrieve component information:\n\n" + String.join("\n", collect));
      });
    }
  }
}
