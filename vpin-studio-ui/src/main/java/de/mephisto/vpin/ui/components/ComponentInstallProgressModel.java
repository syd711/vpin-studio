package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentInstallation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class ComponentInstallProgressModel extends ProgressModel<ComponentInstallation> {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentInstallProgressModel.class);

  private final List<ComponentInstallation> components;
  private final Iterator<ComponentInstallation> iterator;
  private final boolean simulate;

  public ComponentInstallProgressModel(ComponentInstallation component, boolean simulate) {
    super("");
    if(simulate) {
      super.setTitle("Installation Simulator for " + component.getArtifactName());
    }
    else {
      super.setTitle("Installing " + component.getArtifactName());
    }

    this.components = Arrays.asList(component);
    this.iterator = components.iterator();
    this.simulate = simulate;
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
    return true;
  }

  @Override
  public String nextToString(ComponentInstallation c) {
    if (simulate) {
      return "Simulating Installation of " + c.getArtifactName();
    }
    return "Updating " + c.getArtifactName();
  }

  @Override
  public int getMax() {
    return components.size();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, ComponentInstallation next) {
    try {
      ComponentActionLogRepresentation install = null;
      if (simulate) {
        install = client.getComponentService().simulate(next);
      }
      else {
        install = client.getComponentService().install(next);
      }
      progressResultModel.getResults().add(install);
    } catch (Exception e) {
      LOG.error("Failed to run installation: " + e.getMessage(), e);
      Platform.runLater(() -> {
        if (simulate) {
          WidgetFactory.showAlert(Studio.stage, "Component Installation Simulation Failed", "Failed to simulate installation update for " + next + ": " + e.getMessage());
        }
        else {
          WidgetFactory.showAlert(Studio.stage, "Component Installation Failed", "Failed install update for " + next + ": " + e.getMessage());
        }
      });
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
