package de.mephisto.vpin.ui;

import javafx.scene.Parent;
import javafx.scene.layout.Pane;

public class NavigationView {
  private Parent root;
  private final NavigationItem item;
  private final Class controllerClass;
  private Pane navigationButton;
  private String fxml;
  private StudioFXController controller;

  public NavigationView(NavigationItem item, Class controllerClass, Pane navigationButton, String fxml) {
    this.item = item;
    this.controllerClass = controllerClass;
    this.navigationButton = navigationButton;
    this.fxml = fxml;
  }

  public NavigationItem getItem() {
    return item;
  }

  public Parent getRoot() {
    return root;
  }

  public void setRoot(Parent root) {
    this.root = root;
  }

  public StudioFXController getController() {
    return controller;
  }

  public void setController(StudioFXController controller) {
    this.controller = controller;
  }

  public Class getControllerClass() {
    return controllerClass;
  }

  public Pane getNavigationButton() {
    return navigationButton;
  }

  public String getFxml() {
    return fxml;
  }
}
