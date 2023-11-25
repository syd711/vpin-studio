package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class ComponentInstallProgressModel extends ProgressModel<ComponentType> {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentInstallProgressModel.class);

  private final Iterator<ComponentType> iterator;
  private final boolean simulate;
  private final String artifactName;
  private final List<ComponentType> components;

  public ComponentInstallProgressModel(ComponentType type, boolean simulate, String artifactName) {
    super("");
    if(simulate) {
      super.setTitle("Installation Simulator for " + artifactName);
    }
    else {
      super.setTitle("Installing " + artifactName);
    }

    this.components = Arrays.asList(type);
    this.iterator = components.iterator();
    this.simulate = simulate;
    this.artifactName = artifactName;
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
    return true;
  }

  @Override
  public String nextToString(ComponentType c) {
    if (simulate) {
      return "Simulating Installation of " + c;
    }
    return "Updating " + c;
  }

  @Override
  public int getMax() {
    return components.size();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, ComponentType next) {
    try {
      ComponentActionLogRepresentation install = null;
      if (simulate) {
        install = client.getComponentService().simulate(next, artifactName);
      }
      else {
        install = client.getComponentService().install(next, artifactName);
      }
      progressResultModel.getResults().add(install);
    } catch (Exception e) {
      LOG.error("Failed to run installation: " + e.getMessage(), e);
      if (simulate) {
        WidgetFactory.showAlert(Studio.stage, "Component Installation Simulation Failed", "Failed simulate installation update for  " + next + ": " + e.getMessage());
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Component Installation Failed", "Failed install update for  " + next + ": " + e.getMessage());
      }
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
