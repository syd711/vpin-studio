package de.mephisto.vpin.ui.launcher;

import de.mephisto.vpin.commons.ServerInstallationUtil;
import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.*;
import de.mephisto.vpin.commons.utils.ConnectionEntry.ConnectionType;
import de.mephisto.vpin.commons.utils.network.WakeOnLan;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.system.SystemId;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static de.mephisto.vpin.ui.Studio.client;

public class LauncherController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(LauncherController.class);

  @FXML
  private Label studioLabel;

  @FXML
  private Label versionLabel;

  @FXML
  private Button connectBtn;

  @FXML
  private Button refreshBtn;

  @FXML
  private Button installBtn;

  @FXML
  private Button newConnectionBtn;

  @FXML
  private BorderPane main;

  @FXML
  private TableColumn<VPinConnection, String> avatarColumn;

  @FXML
  private TableColumn<VPinConnection, String> nameColumn;

  @FXML
  private TableColumn<VPinConnection, String> hostColumn;

  @FXML
  private TableColumn<VPinConnection, String> statusColumn;

  @FXML
  private TableColumn<VPinConnection, String> actionColumn;

  @FXML
  private TableView<VPinConnection> tableView;

  @FXML
  private Hyperlink helpBtn;

  private ObservableList<VPinConnection> data;

  private Stage stage;
  private ConnectionProperties connectionProperties;
  private final DiscoveryListener discoveryListener = new DiscoveryListener();

  @FXML
  private void onHelp() {
    if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      try {
        Desktop.getDesktop().browse(new URI("https://github.com/syd711/vpin-studio/wiki/Troubleshooting"));
      } catch (Exception ex) {
        LOG.error("Failed to open link: {}", ex.getMessage(), ex);
      }
    }
  }

  @FXML
  private void onInstall() {
    boolean dotNetInstalled = false;
    try {
      dotNetInstalled = WinRegistry.isDotNetInstalled();
    } catch (Exception e) {
      LOG.error("Error checking .net framework: {}", e.getMessage(), e);
      WidgetFactory.showAlert(stage, "Error", "Error checking .net framework: " + e.getMessage());
    }

    if (!dotNetInstalled) {
      WidgetFactory.showAlert(stage, "Error", "No .NET framework > 3.5 found.",
          "The .NET framework is required for some server operations.");
      return;
    }

    boolean install = Dialogs.openInstallerDialog();
    if (install) {
      installServer();
    }
  }

  @FXML
  private void onConnectionRefresh() {
    LOG.info("refresh Connections");
    refreshBtn.setDisable(true);
    connectBtn.setDisable(true);
    newConnectionBtn.setDisable(true);

    stage.getScene().setCursor(Cursor.WAIT);
    new Thread(() -> {
      List<VPinConnection> result = new ArrayList<>();

      LOG.info("Checking connection to localhost");
      VPinConnection connection = checkConnection("localhost");
      if (connection != null) {
        result.add(connection);
      }

      LOG.info("Checking connection to configured connections");
      List<ConnectionEntry> entries = connectionProperties.getConnections();
      for (ConnectionEntry entry : entries) {
        String ipAddress = entry.getIp();
        LOG.info("Checking connection to {}", ipAddress);

        connection = checkConnection(ipAddress);

        if (connection != null) {
          connection.setConnectionId(entry.getId());
          connection.setDiscovered(entry.getType().equals(ConnectionEntry.ConnectionType.DISCOVERED));
          result.add(connection);
        } else {
          // Create a placeholder for an asleep connection
          VPinConnection maybeAsleepConnection = new VPinConnection();
          maybeAsleepConnection.setConnectionId(entry.getId());
          maybeAsleepConnection.setHost(ipAddress);
          maybeAsleepConnection.setName(entry.getName()); // Use stored name from properties
          maybeAsleepConnection.setAvatar(new Image(Studio.class.getResourceAsStream("avatar-default.png"))); // Default
          maybeAsleepConnection.setDiscovered(entry.getType().equals(ConnectionEntry.ConnectionType.DISCOVERED));
          maybeAsleepConnection.setMaybeAsleep(true);
          maybeAsleepConnection.setMacAddress(entry.getMacAddress());
          LOG.info("Connection to {} is asleep or unreachable.", ipAddress);
          result.add(maybeAsleepConnection);
        }
      }

      LOG.info("Checking connection to broadcasted connections");
      for (Map.Entry<InetAddress, BroadcastInfo> entry : discoveryListener.getBroadcasts().entrySet()) {
        InetAddress ip = entry.getKey();
        BroadcastInfo info = entry.getValue();
        String systemName = info.getSystemName();

        LOG.info("Checking broadcasted connection to {} with system name {}", ip.getHostAddress(), systemName);

        connection = checkConnection(ip.getHostAddress());
        if (connection != null) {
          connection.setDiscovered(true);

          LOG.info("Adding discovered connection: {}", ip.getHostAddress());

          // Ensure the connection isn't duplicated in the result list
          VPinConnection finalConnection = connection;
          if (result.stream().noneMatch(conn -> conn.getHost().equals(finalConnection.getHost()))) {
            result.add(connection);
          }
        }
      }

      Platform.runLater(() -> {
        stage.getScene().setCursor(Cursor.DEFAULT);
        data.clear();
        data.addAll(result);

        refreshBtn.setDisable(false);
        newConnectionBtn.setDisable(false);
      });
    }).start();
  }

  @FXML
  private void onNewConnection() {
    String host = WidgetFactory.showInputDialog(stage, "New VPin Studio Connection", "IP or Hostname",
        "Enter the IP address or the hostname to connect to.", null, null);
    if (!StringUtils.isEmpty(host)) {
      boolean found = false;
      for (VPinConnection vpinConnection : data) {
        if (vpinConnection.getHost().equals(host)) {
          found = true;
          break;
        }
      }

      if (found) {
        WidgetFactory.showAlert(stage, "A VPin Studio Connection already exists for '" + host + "'.");
      } else {
        refreshBtn.setDisable(true);
        connectBtn.setDisable(true);
        newConnectionBtn.setDisable(true);

        stage.getScene().setCursor(Cursor.WAIT);
        new Thread(() -> {
          VPinConnection connection = checkConnection(host);
          Platform.runLater(() -> {
            stage.getScene().setCursor(Cursor.DEFAULT);
            if (connection != null) {
              connectionProperties.upsertConnection(host, connection.getName(), connection.getMacAddress(),
                  ConnectionEntry.ConnectionType.CREATED);
              data.add(connection);
            } else {
              WidgetFactory.showAlert(stage, "No service found for '" + host + "'.",
                  "Please check the IP or hostname and try again.");
            }

            refreshBtn.setDisable(false);
            newConnectionBtn.setDisable(false);
          });
        }).start();
      }
    }
  }

  @FXML
  private void onConnect() {
    VPinConnection selectedConnection = tableView.getSelectionModel().getSelectedItem();
    VPinStudioClient client = new VPinStudioClient(selectedConnection.getHost());
    String clientVersion = Studio.getVersion();
    String serverVersion = client.getSystemService().getVersion();

    if (serverVersion != null) {
      // If this connection was discovered add it to our properties.
      if (selectedConnection.getDiscovered()) {
        connectionProperties.upsertConnection(
            selectedConnection.getHost(),
            selectedConnection.getName(),
            selectedConnection.getMacAddress(),
            ConnectionType.DISCOVERED);
      }

      if (!serverVersion.equals(clientVersion)) {
        Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Incompatible Version",
            "The VPin Server you are connecting to has version " + serverVersion + ".", "Please update your client.",
            "Install Update");
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
          // Launch update of the remote client
          Dialogs.openUpdateDialog(client);
        }
        return;
      }

      stage.close();
      Studio.loadStudio(WidgetFactory.createStage(), client);
    }
  }

  @FXML
  private void onWakeOnLan(VPinConnection connection) {
    Integer connectionId = connection.getConnectionId();
    if (connectionId != null) {
      ConnectionEntry connectionEntry = connectionProperties.getConnection(connectionId);
      if (connectionEntry != null && !connectionEntry.getMacAddress().isEmpty()) {
        try {
          WakeOnLan.sendMagicPacket(connectionEntry.getIp(), connectionEntry.getMacAddress(), connectionEntry.getMagicPacketPort());
        } catch (Exception e) {
          LOG.error("WakeOnLan failed: {}", e.getMessage(), e);
          WidgetFactory.showAlert(stage, "WakeOnLan failed.", "Error: " + e.getMessage());
        }
      }
    }
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    this.onConnectionRefresh();

    // Start listening when the window opens
    discoveryListener.startBroadcastListener();

    // Stop the listener when the window is closed
    stage.setOnHidden(event -> discoveryListener.stopBroadcastListener());
    stage.setOnCloseRequest(event -> discoveryListener.stopBroadcastListener());
  }

  private void installServer() {
    try {
      ServerInstallationUtil.install();
      Updater.restartServer();
      main.getTop().setVisible(false);

      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      BorderPane loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Installing Server, waiting for initial connect...");

      main.setCenter(loadingOverlay);

      new Thread(() -> {
        while (client.getSystemService().getVersion() == null) {
          try {
            LOG.info("Waiting for server...");
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            LOG.error("server wait error");
          }
        }

        LOG.info("Found server startup, running on version {}, starting table scan.",
            client.getSystemService().getVersion());
        Platform.runLater(() -> {
          stage.close();
          ProgressDialog.createProgressDialog(new ServiceInstallationProgressModel(Studio.client));
          Studio.loadStudio(WidgetFactory.createStage(), client);
        });
      }).start();
    } catch (Exception e) {
      LOG.error("Server installation failed: {}", e.getMessage(), e);
      WidgetFactory.showAlert(stage, "Server installation failed.", "Error: " + e.getMessage());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("                 No connections found.\n" +
        "Install the service or connect to another system."));

    this.installBtn.setVisible(ServerInstallationUtil.SERVER_EXE.exists());
    this.installBtn.setDisable(client.getSystemService().getVersion() != null);
    this.helpBtn.setVisible(ServerInstallationUtil.SERVER_EXE.exists());

    connectBtn.setDisable(true);
    tableView.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> connectBtn.setDisable(newValue == null));

    Font font = Font.font("Impact", FontPosture.findByName("regular"), 28);
    studioLabel.setFont(font);
    versionLabel.setText(Studio.getVersion());

    List<VPinConnection> connections = new ArrayList<>();
    data = FXCollections.observableList(connections);
    tableView.setItems(data);

    nameColumn.setCellValueFactory(cellData -> {
      VPinConnection value = cellData.getValue();
      return new SimpleObjectProperty(value.getName());
    });

    hostColumn.setCellValueFactory(cellData -> {
      VPinConnection value = cellData.getValue();
      return new SimpleObjectProperty(value.getHost());
    });

    avatarColumn.setCellValueFactory(cellData -> {
      VPinConnection value = cellData.getValue();
      ImageView view = new ImageView(value.getAvatar());
      view.setPreserveRatio(true);
      view.setFitWidth(50);
      view.setFitHeight(50);
      CommonImageUtil.setClippedImage(view, (int) (value.getAvatar().getWidth() / 2));
      return new SimpleObjectProperty(view);
    });

    statusColumn.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);

        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
          setGraphic(null);
          return;
        }

        VPinConnection connection = getTableRow().getItem();

        HBox statusBox = new HBox();
        statusBox.setSpacing(5); // Space between icons
        statusBox.setAlignment(Pos.CENTER_LEFT);

        // Add discovered icon (if discovered)
        if (connection.getDiscovered()) {
          FontIcon discoveredIcon = new FontIcon("mdi2a-antenna");
          discoveredIcon.setIconSize(18);
          discoveredIcon.setIconColor(Paint.valueOf(WidgetFactory.OK_COLOR)); // Green for discovered
          Tooltip discoveredTooltip = new Tooltip("This connection was discovered on the local network.");
          Tooltip.install(discoveredIcon, discoveredTooltip); // Attach tooltip to the icon
          statusBox.getChildren().add(discoveredIcon);
        }

        // Add WOL button (if asleep) and we have a MAC Address
        if (connection.isMaybeAsleep() && connection.getMacAddress() != null && !connection.getMacAddress().isEmpty()
            && !"127.0.0.1".equals(connection.getHost())) {
          Button wolButton = new Button();
          FontIcon wolIcon = new FontIcon("mdi2w-wifi-arrow-right");
          wolIcon.setIconSize(18);
          wolIcon.setIconColor(Paint.valueOf("#008080")); // Teal for WOL
          wolButton.setGraphic(wolIcon);
          wolButton.getStyleClass().add("wol-button");
          stage.getScene().getStylesheets().add(getClass().getResource("scene-launcher.css").toExternalForm());
          Tooltip wolTooltip = new Tooltip("Send a Wake-on-LAN signal to wake this device.");
          Tooltip.install(wolButton, wolTooltip); // Attach tooltip to the button

          wolButton.setOnAction(event -> {
            Optional<ButtonType> result = WidgetFactory.showConfirmation(stage,
                "Send WOL packet to " + connection.getHost() + "?");
            if (result.isPresent() && result.get().equals(ButtonType.OK)) {
              onWakeOnLan(connection);
              onConnectionRefresh();
            }

            LOG.info("Sending WOL packet to {}", connection.getHost());
          });
          statusBox.getChildren().add(wolButton);
        }

        setGraphic(statusBox);
      }
    });

    actionColumn.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);

        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
          setGraphic(null);
          return;
        }

        VPinConnection connection = getTableRow().getItem();

        if (connection.getConnectionId() == null) {
          return;
        }

        HBox actionBox = new HBox();
        actionBox.setStyle("-fx-alignment: center;");
        actionBox.setSpacing(20); // Extra spacing for delete button

        // Add delete icon
        FontIcon deleteIcon = new FontIcon("mdi2d-delete-outline");
        deleteIcon.setIconSize(16);
        deleteIcon.setIconColor(Paint.valueOf("#F08080")); // Red for delete

        Button deleteButton = new Button();
        deleteButton.setGraphic(deleteIcon);
        deleteButton.setStyle("-fx-border-radius: 6px; -fx-cursor: hand;");
        deleteButton.setOnAction(event -> {
          Optional<ButtonType> result = WidgetFactory.showConfirmation(stage,
              "Delete connection to '" + connection.getHost() + "'?");
          if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            connectionProperties.removeConnection(connection.getHost());
            onConnectionRefresh();
          }
        });

        // Add tooltip to the delete button
        Tooltip deleteTooltip = new Tooltip("Permanently delete this connection.");
        Tooltip.install(deleteButton, deleteTooltip);

        actionBox.getChildren().add(deleteButton);
        setGraphic(actionBox);
      }
    });

    tableView.setRowFactory(tv -> {
      TableRow<VPinConnection> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
          onConnect();
        }
      });
      return row;
    });

    // Initialize our connection properties object.
    connectionProperties = new ConnectionProperties();

    // Initialize our Discovery Broadcaster.
    discoveryListener.setBroadcastDataChangeListener(this::onConnectionRefresh);
  }

  private VPinConnection checkConnection(String host) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<VPinConnection> future = executor.submit(() -> {
      VPinStudioClient client = new VPinStudioClient(host);
      SystemId id = client.getSystemService().getSystemId();
      if (id != null && id.getVersion() != null) {
        VPinConnection connection = new VPinConnection();
        connection.setName(id.getSystemName());
        connection.setHost(host);

        // Detect MAC Address
        String macAddress = detectMacAddressViaArp(host);
        connection.setMacAddress(macAddress);

        InputStream av = client.getAssetService().getAvatar(false);
        if (av == null) {
          av = ServerFX.class.getResourceAsStream("avatar-default.png");
        }
        Image image = new Image(av);
        connection.setAvatar(image);

        return connection;
      }
      return null;
    });

    VPinConnection connection = null;
    try {
      connection = future.get(3000, TimeUnit.MILLISECONDS);
      LOG.info("connection to {} can be established", host);
    } catch (Exception e) {
      LOG.info("connection to {} took too long to answer", host);
    }
    executor.shutdownNow();
    return connection;
  }

  private String detectMacAddressViaArp(String host) {
    try {
      // Run the 'arp -a' command
      Process process = Runtime.getRuntime().exec("arp -a " + host);
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        if (line.contains(host)) {
          LOG.info("ARP entry found for host: {}", host);

          // Parse the MAC address from the output
          String[] tokens = line.split("\\s+"); // Handles spaces and tabs
          for (String token : tokens) {
            // Match standard MAC address format
            if (token.matches("([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}")) {
              LOG.info("MAC Address detected for host {}: {}", host, token.toUpperCase());
              return token.toUpperCase();
            }
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Error detecting MAC Address for host {}: {}", host, e.getMessage());
    }

    LOG.warn("No MAC Address found for host: {}", host);
    return "";
  }
}
