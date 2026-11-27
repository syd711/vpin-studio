package de.mephisto.vpin.ui.tables.panels;

import java.util.Locale;
import java.util.function.Predicate;

import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import org.jspecify.annotations.NonNull;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

  /**
   * The table is moved by this width, the panel itself is 14px narrower (prefWidth in the FXML)
   * to keep the split view gap between filter panel and table.
   */
  public static final int FILTER_WIDTH = 300;

  protected boolean visible = false;

  @FXML
  protected VBox filterRoot;

  /** "Reset All" button in the panel header, enabled while a filter is set */
  @FXML
  private Button resetBtn;

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
    // Debounced: filtering may now involve a server round-trip (see TableFilterController),
    // so avoid firing a request per keystroke.
    PauseTransition debounce = new PauseTransition(Duration.millis(300));
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      clearBtn.setVisible(filterValue != null && !filterValue.isEmpty());
      searchTerm = filterValue;
      debounce.setOnFinished(e -> {
        tableController.clearSelection();
        tableController.applyFilter();
      });
      debounce.playFromStart();
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
    // the gap between the opened filter panel and the table must show the page background,
    // not a panel background set on the table stack
    stackPane.setStyle("-fx-background-color: transparent;");
    upperCaseCaptions(filterRoot);

    stackPane.getChildren().addFirst( filterRoot);
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
      filteredTable.setMaxWidth(stackPane.getWidth() - FILTER_WIDTH);
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
      if (resetBtn != null) {
        resetBtn.setDisable(!hasFilter());
      }
      filterRoot.setVisible(true);
      filterButton.setGraphic(WidgetFactory.createIcon("mdi2f-filter-menu"));
      TranslateTransition filterTransition = TransitionUtil.createTranslateByXTransition(filteredTable, 300, FILTER_WIDTH);
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
      TranslateTransition translateByXTransition = TransitionUtil.createTranslateByXTransition(filteredTable, 300, -FILTER_WIDTH);
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
    if (resetBtn != null) {
      resetBtn.setDisable(!hasFilter);
    }
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

  /**
   * Section captions (sm-caption) are upper case like the captions of the details panels,
   * the resource bundles keep the title case texts.
   */
  private static void upperCaseCaptions(Node node) {
    if (node instanceof Label label && label.getStyleClass().contains("sm-caption") && label.getText() != null) {
      label.setText(label.getText().toUpperCase(Locale.getDefault()));
    }
    if (node instanceof ScrollPane scrollPane) {
      upperCaseCaptions(scrollPane.getContent());
    }
    else if (node instanceof Parent parent) {
      for (Node child : parent.getChildrenUnmodifiable()) {
        upperCaseCaptions(child);
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
