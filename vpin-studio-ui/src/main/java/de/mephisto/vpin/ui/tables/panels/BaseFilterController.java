package de.mephisto.vpin.ui.tables.panels;

import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Common code for toggling FilterPane with animation
 */
public class BaseFilterController {

  protected boolean visible = false;

  private Pane filterRoot;

  private Button filterButton;

  private StackPane stackPane;
  
  private TableView<?> filteredTable;

  private boolean blocked;

  /**
   * Setup the drawer, bind events and properties
   * @param filterRoot The Pane containing the filters that is togggled
   * @param filterButton The Button to open / close the filter pane 
   * @param stackPane The StackPane that contains the table, in which filter pane is added
   * @param filteredTable The table that is filtered
   */
  protected void setupDrawer(Pane filterRoot, Button filterButton, StackPane stackPane, TableView<?> filteredTable) {
    this.filterRoot = filterRoot;
    this.filterButton = filterButton;
    this.stackPane = stackPane;
    this.filteredTable = filteredTable;
  
    filterRoot.setVisible(false);

    stackPane.setAlignment(Pos.TOP_LEFT);

    stackPane.getChildren().add(0, filterRoot);
    filterRoot.prefHeightProperty().bind(stackPane.heightProperty());
    //titlePaneRoot.prefHeightProperty().bind(stackPane.heightProperty());
    stackPane.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (newValue!=null) {
          stackPane.setMinWidth(newValue.doubleValue());
          stackPane.setMaxWidth(newValue.doubleValue());
        }    
        refreshState();
      }
    });
  }

  private void refreshState() {
    if (visible) {
      filteredTable.setMaxWidth(stackPane.getWidth() - 250);
    }
    else {
      filteredTable.setMaxWidth(stackPane.getWidth());
    }
  }

  /**
   * Open / close the filter pane with animation
   */
  public void toggleDrawer() {
    if (blocked) {
      return;
    }

    blocked = true;

    if (!visible) {
      visible = true;
      filterRoot.setVisible(true);
      filterButton.setGraphic(WidgetFactory.createIcon("mdi2f-filter-menu"));
      TranslateTransition filterTransition = TransitionUtil.createTranslateByXTransition(filteredTable, 300, 250);
      filterTransition.statusProperty().addListener(new ChangeListener<Animation.Status>() {
        @Override
        public void changed(ObservableValue<? extends Animation.Status> observable, Animation.Status oldValue, Animation.Status newValue) {
          if (newValue == Animation.Status.STOPPED) {
            refreshState();
            blocked = false;
          }
        }
      });
      filterTransition.play();
    }
    else {
      visible = false;
      filterButton.setGraphic(WidgetFactory.createIcon("mdi2f-filter-menu-outline"));
      TranslateTransition translateByXTransition = TransitionUtil.createTranslateByXTransition(filteredTable, 300, -250);
      translateByXTransition.statusProperty().addListener(new ChangeListener<Animation.Status>() {
        @Override
        public void changed(ObservableValue<? extends Animation.Status> observable, Animation.Status oldValue, Animation.Status newValue) {
          if (newValue == Animation.Status.STOPPED) {
            filterRoot.setVisible(false);
            blocked = false;
          }
        }
      });
      translateByXTransition.play();
      refreshState();
    }
  }

  /**
   * Light on (hasFilter=true) or off the filter button
   * @param hasFilter Whether a Filter is set or not 
   */
  public void toggleFilterButton(boolean hasFilter) {
    if (!hasFilter) {
      filterButton.getStyleClass().remove("toggle-button-selected");
      filterRoot.getStyleClass().remove("toggle-selected");
    }
    else {
      filterRoot.getStyleClass().add("toggle-selected");
      if (!filterButton.getStyleClass().contains("toggle-button-selected")) {
        filterButton.getStyleClass().add("toggle-button-selected");
      }
    }
  }

}
