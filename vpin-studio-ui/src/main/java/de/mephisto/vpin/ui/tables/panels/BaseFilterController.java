package de.mephisto.vpin.ui.tables.panels;

import java.util.function.Predicate;

import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Common code for toggling FilterPane with animation
 */
public abstract class BaseFilterController<T, M extends BaseLoadingModel<T, M>> {

  protected boolean visible = false;

  @FXML
  protected VBox filterRoot;

  private Button filterButton;

  private StackPane stackPane;
  
  private TableView<?> filteredTable;

  private boolean blocked;

  protected boolean updatesDisabled = false;

  private String searchTerm;

  private PlaylistRepresentation playlist;

  protected BaseTableController<T, M> tableController;

  protected FilterSettings filterSettings;

  //--------------------------------------
  // Initialisation

  public void setTableController(BaseTableController<T, M> tableController) {
    this.tableController = tableController;
  }

  public void bindSearchField(TextField textfieldSearch, Button clearBtn) {
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      tableController.clearSelection();
      searchTerm = filterValue;
      tableController.applyFilter();
      clearBtn.setVisible(filterValue != null && !filterValue.isEmpty());
    });
  }

  public void bindPlaylistField(ComboBox<PlaylistRepresentation> playlistCombo) {
    playlistCombo.valueProperty().addListener(new ChangeListener<PlaylistRepresentation>() {
      @Override
      public void changed(ObservableValue<? extends PlaylistRepresentation> observableValue, PlaylistRepresentation old, PlaylistRepresentation t1) {
        playlist = t1;
        tableController.applyFilter();
      }
    });
  }

  //--------------------------------------
  // Drawer management

  /**
   * Setup the drawer, bind events and properties
   * @param filterButton The Button to open / close the filter pane 
   * @param stackPane The StackPane that contains the table, in which filter pane is added
   * @param filteredTable The table that is filtered
   */
  protected void setupDrawer(Button filterButton, StackPane stackPane, TableView<M> filteredTable) {
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
        refreshState();
      }
    });
  }

  protected void refreshState() {
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
  @FXML
  public void toggle() {
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

  //--------------------------------------

  @FXML
  private void onReset() {
    updatesDisabled = true;
    resetFilters();
    updatesDisabled = false;
    tableController.applyFilter();
  }

  protected abstract void resetFilters();

  public void applyFilters() {
    toggleFilterButton(hasFilter());
    if (updatesDisabled) {
      return;
    }
    updatesDisabled = true;
    Platform.runLater(() -> {
      tableController.applyFilter();
      updatesDisabled = false;
    });
  }

  protected abstract boolean hasFilter();

  public void loadFilterSettings(@NonNull FilterSettings filterSettings) {

  }

  public final Predicate<M> buildPredicate() {
    return buildPredicate(searchTerm, playlist);
  }
  public abstract Predicate<M> buildPredicate(String searchTerm, PlaylistRepresentation playlist);

}
