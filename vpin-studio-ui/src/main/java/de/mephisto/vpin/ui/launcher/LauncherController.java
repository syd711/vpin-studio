package de.mephisto.vpin.ui.launcher;

import de.mephisto.vpin.commons.ServerInstallationUtil;
import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.*;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
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
import java.io.File;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

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
  private TableColumn<VPinConnection, String> actionColumn;

  @FXML
  private TableView<VPinConnection> tableView;


  @FXML
  private Hyperlink helpBtn;

  private ObservableList<VPinConnection> data;

  private Stage stage;
  private PropertiesStore store;

  private final Map<InetAddress, BroadcastInfo> broadcastData = new ConcurrentHashMap<>();

  @FXML
  private void onHelp() {
    if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      try {
        Desktop.getDesktop().browse(new URI("https://github.com/syd711/vpin-studio/wiki/Troubleshooting"));
      }
      catch (Exception ex) {
        LOG.error("Failed to open link: {}", ex.getMessage(), ex);
      }
    }
  }

  @FXML
  private void onInstall() {
    boolean dotNetInstalled = false;
    try {
      dotNetInstalled = WinRegistry.isDotNetInstalled();
    }
    catch (Exception e) {
      LOG.error("Error checking .net framework: {}", e.getMessage(), e);
      WidgetFactory.showAlert(stage, "Error", "Error checking .net framework: " + e.getMessage());
    }

    if (!dotNetInstalled) {
      WidgetFactory.showAlert(stage, "Error", "No .NET framework > 3.5 found.", "The .NET framework is required for some server operations.");
      return;
    }

    boolean install = Dialogs.openInstallerDialog();
    if (install) {
      installServer();
    }
  }

  @FXML
  private void onConnectionRefresh() {
    refreshBtn.setDisable(true);
    connectBtn.setDisable(true);
    newConnectionBtn.setDisable(true);

    stage.getScene().setCursor(Cursor.WAIT);
    new Thread(() -> {
      List<VPinConnection> result = new ArrayList<>();

      VPinConnection connection = checkConnection("localhost");
      if (connection != null) {
        result.add(connection);
      }

      List<Object> entries = store.getEntries();
      for (Object entry : entries) {
        LOG.info("Checking connection to {}", entry);
        connection = checkConnection(String.valueOf(entry));
        if (connection != null) {
          result.add(connection);
        }
      }

      for (Map.Entry<InetAddress, BroadcastInfo> entry : broadcastData.entrySet()) {
        InetAddress ip = entry.getKey();
        BroadcastInfo info = entry.getValue();
        String systemName = info.getSystemName();

        LOG.info("Checking broadcasted connection to {} with system name {}", ip.getHostAddress(), systemName);
        connection = checkConnection(ip.getHostAddress());
        VPinConnection finalConnection = connection;
        if (connection != null && result.stream().noneMatch(conn -> conn.getHost().equals(finalConnection.getHost()))) {
          connection.setDiscovered(true);
          result.add(connection);
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
    String host = WidgetFactory.showInputDialog(stage, "New VPin Studio Connection", "IP or Hostname", "Enter the IP address or the hostname to connect to.", null, null);
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
      }
      else {
        refreshBtn.setDisable(true);
        connectBtn.setDisable(true);
        newConnectionBtn.setDisable(true);

        stage.getScene().setCursor(Cursor.WAIT);
        new Thread(() -> {
          VPinConnection connection = checkConnection(host);
          Platform.runLater(() -> {
            stage.getScene().setCursor(Cursor.DEFAULT);
            if (connection != null) {
              store.set(String.valueOf(store.getEntries().size()), host);
              data.add(connection);
            }
            else {
              WidgetFactory.showAlert(stage, "No service found for '" + host + "'.", "Please check the IP or hostname and try again.");
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
    VPinConnection selectedItem = tableView.getSelectionModel().getSelectedItem();
    VPinStudioClient client = new VPinStudioClient(selectedItem.getHost());
    String clientVersion = Studio.getVersion();
    String serverVersion = client.getSystemService().getVersion();

    if (serverVersion != null) {
      if (!serverVersion.equals(clientVersion)) {
        Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Incompatible Version", "The VPin Server you are connecting to has version " + serverVersion + ".", "Please update your client.", "Install Update");
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
          Dialogs.openUpdateDialog();
        }
        return;
      }

      stage.close();
      Studio.loadStudio(WidgetFactory.createStage(), client);
    }
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    this.onConnectionRefresh();

    // Start listening when the window opens
    this.startBroadcastListener();

    // Stop the listener when the window is closed
    stage.setOnHidden(event -> this.stopBroadcastListener());
    stage.setOnCloseRequest(event -> this.stopBroadcastListener());
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
          }
          catch (InterruptedException e) {
            LOG.error("server wait error");
          }
        }

        LOG.info("Found server startup, running on version {}, starting table scan.", client.getSystemService().getVersion());
        Platform.runLater(() -> {
          stage.close();
          ProgressDialog.createProgressDialog(new ServiceInstallationProgressModel(Studio.client));
          Studio.loadStudio(WidgetFactory.createStage(), client);
        });
      }).start();
    }
    catch (Exception e) {
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
    tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> connectBtn.setDisable(newValue == null));

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

    actionColumn.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);

        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
          setGraphic(null);
          return;
        }

        VPinConnection value = getTableRow().getItem();

        // No icon for localhost
        if (value.getHost().equals("localhost")) {
          setGraphic(null);
        } else if (value.getDiscovered()) {
          // Show a custom icon for discovered connections
          FontIcon discoveredIcon = new FontIcon("mdi2a-antenna");
          discoveredIcon.setIconSize(16);
          discoveredIcon.setIconColor(Paint.valueOf("#2196F3"));
          setGraphic(discoveredIcon);
        } else {
          // Show the delete button for non-discovered connections
          Button button = new Button();
          button.setStyle("-fx-border-radius: 6px;");
          FontIcon deleteIcon = new FontIcon("mdi2d-delete-outline");
          deleteIcon.setIconSize(8);
          deleteIcon.setIconColor(Paint.valueOf("#FFFFFF"));
          button.setGraphic(deleteIcon);
          button.setOnAction(event -> {
            Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete connection to '" + value.getHost() + "'?");
            if (result.isPresent() && result.get().equals(ButtonType.OK)) {
              store.removeValue(String.valueOf(value.getHost()));
              onConnectionRefresh();
            }
          });
          setGraphic(button);
        }
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

    File propertiesFile = new File("config/connection.properties");
    propertiesFile.getParentFile().mkdirs();
    store = PropertiesStore.create(propertiesFile);
  }

  private VPinConnection checkConnection(String host) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<VPinConnection> future = executor.submit(() -> {
      VPinStudioClient client = new VPinStudioClient(host);
      String version = client.getSystemService().getVersion();
      if (version != null) {
        VPinConnection connection = new VPinConnection();
        PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
        PreferenceEntryRepresentation systemName = client.getPreference(PreferenceNames.SYSTEM_NAME);

        String name = systemName.getValue();
        if (StringUtils.isEmpty(name)) {
          name = UIDefaults.VPIN_NAME;
        }
        connection.setHost(host);
        connection.setName(name);

        if (!StringUtils.isEmpty(avatarEntry.getValue())) {
          connection.setAvatar(new Image(client.getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue())));
        }
        else {
          Image image = new Image(Studio.class.getResourceAsStream("avatar-default.png"));
          connection.setAvatar(image);
        }
        return connection;
      }
      return null;
    });

    executor.shutdownNow();

    try {
      return future.get(3000, TimeUnit.MILLISECONDS);
    }
    catch (Exception e) {
      //
    }

    return null;
  }

  private Thread broadcastListenerThread;
  private Thread staleEntriesRemoverThread;
  private DatagramSocket socket;
  private boolean shouldListen = false;

  private void listenForBroadcast() {
    try {
      socket = new DatagramSocket(50505);
      byte[] buffer = new byte[256];
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

      while (shouldListen) {
        socket.receive(packet);

        String receivedSystemName = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.US_ASCII);
        if (receivedSystemName.isEmpty()) {
          receivedSystemName = UIDefaults.VPIN_NAME;
        }

        InetAddress senderAddress = packet.getAddress();

        long currentTime = System.currentTimeMillis();
        if (!broadcastData.containsKey(senderAddress)) {
          // Log the received information
            LOG.info("Received broadcast for the first time from IP: {}, System Name: {}", senderAddress.getHostAddress(), receivedSystemName);

          // Store the IP and system name
          broadcastData.put(senderAddress, new BroadcastInfo(receivedSystemName, currentTime));

          // Notify that broadcast data has changed
          onBroadcastDataChanged();
        } else {
          BroadcastInfo existingInfo = broadcastData.get(senderAddress);
          existingInfo.updateLastBroadcastTime(currentTime);
        }
      }
    } catch (Exception e) {
        LOG.error("Error receiving broadcast: {}", e.getMessage(), e);
    }
  }

  public synchronized void startBroadcastListener() {
    if (broadcastListenerThread != null) {
      LOG.info("Broadcast listener already started");
      return;
    }

    shouldListen = true;

    broadcastListenerThread = new Thread(this::listenForBroadcast);

    broadcastListenerThread.start();

    // Start the thread that removes stale entries
    startStaleEntriesRemover();
  }

  public synchronized void stopBroadcastListener() {
    if (broadcastListenerThread == null) {
      LOG.info("Broadcast listener not running");
      return;
    }

    shouldListen = false;

    try {
      broadcastListenerThread.join(); // Wait for the listener thread to stop
    } catch (InterruptedException e) {
      LOG.error("Failed to stop broadcast listener: {}", e.getMessage(), e);
    }

    // Close the DatagramSocket to release the port
    if (socket != null && !socket.isClosed()) {
      socket.close();
    }

    LOG.info("Broadcast listener stopped");
    broadcastListenerThread = null;

    stopStaleEntriesRemover();
  }

  private void onBroadcastDataChanged() {
    Platform.runLater(this::onConnectionRefresh);
  }

  public synchronized void startStaleEntriesRemover() {
    if (staleEntriesRemoverThread != null) {
      LOG.info("Stale entries remover already started");
      return;
    }

    staleEntriesRemoverThread = new Thread(() -> {
      while (shouldListen) { // Stop checking if shouldListen is false
        long currentTime = System.currentTimeMillis();
        boolean dataChanged = false;

        // Iterate over the broadcastData and remove stale entries
        for (InetAddress ip : new ArrayList<>(broadcastData.keySet())) {
          BroadcastInfo info = broadcastData.get(ip);
          if (currentTime - info.getLastBroadcastTime() > 10000) { // More than 10 seconds have passed
              LOG.info("Removing stale broadcast data for IP: {}", ip.getHostAddress());
            broadcastData.remove(ip);
            dataChanged = true;
          }
        }

        if (dataChanged) {
          onBroadcastDataChanged();
        }

        try {
          Thread.sleep(2000); // Check every 2 seconds
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    });

    staleEntriesRemoverThread.start();
  }

  public synchronized void stopStaleEntriesRemover() {
    if (staleEntriesRemoverThread == null) {
      LOG.info("Stale entries remover not running");
      return;
    }

    shouldListen = false; // This will stop the stale entries remover loop

    try {
      staleEntriesRemoverThread.join(); // Wait for the stale entries remover thread to stop
    } catch (InterruptedException e) {
        LOG.error("Failed to stop stale entries remover: {}", e.getMessage(), e);
    }

    LOG.info("Stale entries remover stopped");
    staleEntriesRemoverThread = null;
  }
}
