package de.mephisto.vpin.ui.cards;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.matcher.VpsAutomatcher;
import de.mephisto.vpin.connectors.vps.matcher.VpsDebug;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.ui.HeaderController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.AutoCompleteTextField;
import de.mephisto.vpin.ui.util.AutoMatchModel;
import de.mephisto.vpin.ui.util.PositionSelection;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class InstructionsCardsController  implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(InstructionsCardsController.class);
  
    /** Sligthly increase LOGs, used for debugging of matchings */
    private boolean LOG_VPSMATCHING_DEBUG = true;
  
    public static String TESSERACT_FOLDER = SystemInfo.RESOURCES + "tesseract";
  
    @FXML
    private HeaderController headerController;  //fxml magic! Not unused -> id + "Controller"
  
    @FXML
    private TextField tableNameField;
    private AutoCompleteTextField autoCompleteNameField;
    @FXML
    private ComboBox<String> languagesCombo;
    @FXML
    private CheckBox autosaveCheckbox;
  
    @FXML
    private Button openVpsTableBtn;
    @FXML
    private ToolBar toolbar;
    @FXML
    private Button prevBtn;
    @FXML
    private Button nextBtn;
    
    @FXML
    private Button saveBtn;
  
    @FXML
    private Label tableId;
  
    @FXML
    private Pane imagepane;
    @FXML
    private ImageView imageview;
   
    private BooleanProperty processing = new SimpleBooleanProperty(false);
    private BooleanProperty dirty = new SimpleBooleanProperty(false);
  
    private PositionResizer resizer; 
  
    @FXML
    private TextArea instructionsTextArea;
  
    /** The list of tables used by the matcher */
    private CompletableFuture<VPS> vpsTables = null;
  
    //--------------------------
  
    private File database;
  
    private File rootFolder;
  
    /** The processed image */
    private ObjectProperty<File> imageFile = new SimpleObjectProperty<>();
  
    /** selected VPS Table ID */
    private ObjectProperty<VpsTable> vpsTable = new SimpleObjectProperty<>();
  
    //---------------------------
  
    private @Nonnull VPS getVpsTables() {
      try {
        // wait the database is loaded
        return vpsTables.get();
      }
      catch (Exception ie) {
        return new VPS();
      }
    }
  
    @Override
    public void initialize(URL location, ResourceBundle resources) {
  
      // Load the VPS database
      vpsTables = CompletableFuture.supplyAsync(() -> {
        VPS vps = new VPS();
        vps.reload();
        return vps;
      });
  
      // update title when the dirty flag changes
      dirty.addListener((pbs, o, v) -> updateTitle());
  
    VpsDebug debug = LOG_VPSMATCHING_DEBUG ? new VpsDebug() : null;
    VpsAutomatcher automatcher = new VpsAutomatcher(debug);
    autoCompleteNameField = new AutoCompleteTextField(tableNameField, value -> {
      VPS vps  = getVpsTables();
      Optional<VpsTable> table = vps.getTables().stream().filter(e -> e.getDisplayName().equals(value)).findFirst();
      if (table.isPresent()) {
        load(table.get());
      }
    }, input -> {
      VPS vps  = getVpsTables();
      if (debug != null) {
        debug.clear();
      }
      List<VpsTable> matches = automatcher.autoMatchTables(vps, input);
      if (debug != null) {
        LOG.info(debug.toString());
      }
      return matches.stream().map(e -> new AutoMatchModel(e.getDisplayName(), e.getDisplayName())).collect(Collectors.toList());
    });

    // open VPS button disabled when no VPSTable identified
    openVpsTableBtn.disableProperty().bind(vpsTable.isNull());

    // Fill Locale dropdowns
    ObservableList<String> locales = FXCollections.observableArrayList("EN", "FR");
    languagesCombo.setItems(locales);

    // Add buttons to toolbar
    createButton("Clear", e -> instructionsTextArea.clear(), null);
    createButton("Grab Text", e -> onGrabText(), imageFile.isNull());
    createButton("Grab Image", e -> onGrabImage(), imageFile.isNull());

    prevBtn.disableProperty().bind(imageFile.isNull());
    nextBtn.disableProperty().bind(imageFile.isNull());
    autosaveCheckbox.disableProperty().bind(imageFile.isNull());

    createTransformationButton("To LowerCase", txt -> onLowerCase(txt));

    createTransformationButton("Remove Line Breaks", txt -> onRemoveLineBreaks(txt));

    instructionsTextArea.setWrapText(true);
    instructionsTextArea.disableProperty().bind(vpsTable.isNull());
    instructionsTextArea.textProperty().addListener((obs, o, v) -> dirty.set(true));

    //prevBtn.disableProperty().bind(vpsTable.isNull());
    //nextBtn.disableProperty().bind(vpsTable.isNull());

    // display the id in sidebar
    tableId.textProperty().bind(vpsTable.map(t -> t.getId()));;

    // add a selector in the pane
    Bounds area = new BoundingBox(0, 0, 1920, 1080);

    new PositionSelection(imagepane,
      () -> {
        unselectArea();
      }, 
      rect -> {
        resizer = new PositionResizer();
        resizer.setX((int) rect.getMinX());
        resizer.setY((int) rect.getMinY());
        resizer.setWidth((int) rect.getWidth());
        resizer.setHeight((int) rect.getHeight());
        resizer.setBounds(area);
        resizer.addToPane(imagepane);
        resizer.select();
      });  
  }

  private void createButton(String text, EventHandler<ActionEvent> handler, BooleanBinding extraBindings) {
    Button btn = new Button(text);
    btn.getStyleClass().add("default-button");
    btn.setOnAction(handler);
    toolbar.getItems().add(btn);
    BooleanBinding isDisabled = vpsTable.isNull().or(processing);
    if (extraBindings != null) {
      isDisabled = isDisabled.or(extraBindings);
    }
    btn.disableProperty().bind(isDisabled);
  }
  private void createTransformationButton(String text, Function<String, String> transformation) {
    createButton(text, e -> {
      String txt = instructionsTextArea.getText();
      txt = transformation.apply(txt);
      instructionsTextArea.setText(txt);
    }, null);
  }

  private void setFile(File f) {
    this.imageFile.set(f);;
    updateTitle();

    // empty the table
    vpsTable.set(null);
    instructionsTextArea.clear();
    languagesCombo.setValue(null);
    // if there is a resizer, remove it
    unselectArea();
    dirty.set(false);

    if (f != null) {
      autoCompleteNameField.setText(FilenameUtils.getBaseName(f.getName()));
      autoCompleteNameField.selectIfMatch();

      if (f.exists()) {
        try (InputStream in = new FileInputStream(f)) {
          Image image = new Image(in);
          imageview.setImage(image);
        }
        catch (IOException ioe) {
          LOG.error("Cannot load image " + f, ioe);
        }
      }
      // memorize current file
      setCurrentFile(f);
    }
  }

  private void updateTitle() {
    if (imageFile.get() != null) {
      headerController.setTitle(imageFile.get().getName() + (dirty.get() ? " (*)" : ""));
    }
    else {
      headerController.setTitle("No file found !");
    }
  }

  //---------------------------
  // ACTIONS

  private void onGrabText() {
    processing.set(true);
    JFXFuture
      .supplyAsync(() -> {
        BufferedImage img = getImage();
        return extractText(img);
      })
      .thenAcceptLater(txt -> {
        int pos = instructionsTextArea.getCaretPosition();
        instructionsTextArea.insertText(pos, txt);
        processing.set(false);
      });
  }

  private void onGrabImage() {
    try {
      BufferedImage img = getImage();
      File f = imageFile.get();
      File target = new File(f.getParent(), f.getName() + ".extract.png");
      ImageIO.write(img, "png", target);
      LOG.info("Image grabbed into " + target);
    }
    catch (Exception e) {
      LOG.error("cannot grab image", e);
    }
  }

  private String onLowerCase(String txt) {
    StringBuilder bld = new StringBuilder();

    // toLowerCase() then split by the delimiters, returning the delimeters as token
    StringTokenizer tok = new StringTokenizer(txt.toLowerCase(), ".!?:;\n", true);
    boolean appendSpace = false;
    while (tok.hasMoreTokens()) {
      String s = tok.nextToken();
      // If this is dot or equivalent, append the delimeter with a space
      if (StringUtils.contains(".!?:;", s)) {
        bld.append(s);
        appendSpace = true;  
      }
      // If this is newline, just append the delimeter
      else if ("\n".equals(s)) {
        bld.append(s);
        appendSpace = false;  
      }
      else {
        // do we need to append a space before ?
        if (appendSpace) {
          bld.append(" ");  
          appendSpace = false;
        }
        bld.append(StringUtils.capitalize(s.trim()));
      }
    }
    txt = bld.toString();
    bld.setLength(0);

    // now replace back the patter a-b-c-d to A-B-C-D
    Pattern p = Pattern.compile("\\s(?:\\w\\s?-\\s?)+\\w", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(txt);
    int pos = 0;
    while (m.find()) {
      bld.append(txt.substring(pos, m.start()));
      bld.append(" ").append(m.group().toUpperCase().replace(" ", ""));
      pos = m.end();
    }
    bld.append(txt.substring(pos));

    return bld.toString();
  }

  private String onRemoveLineBreaks(String txt) {
    // trims all the tab or space characters at the beginning and end of each line or of the text.
    txt = txt.replaceAll("(?<=\n|^)[\t ]+|[\t ]+(?=$|\n)", "");
    // replaces all the single line-feed ending with - character with "", effectively joining the lines
    txt = txt.replaceAll("(?<=.)-\n(?=.)", "");
    // replaces all the single line-feed (newline) characters with spaces, effectively joining the lines
    txt = txt.replaceAll("(?<=.)\n(?=.)", " ");
    return txt;
  }

  //-------------------------------------------

  public void setRootFolder(File folder) {
    this.rootFolder = folder;
    setFile(getCurrentFile());
  }

  public void setDatabase(File database) {
    this.database = database;
  }

  public File getCurrentFile() {
    try {
      File indexFile = new File(rootFolder, "_current");
      File[] currFile = new File[] { null };
      String filename = null;
      if (indexFile.exists()) {
          filename = Files.readString(indexFile.toPath());
          currFile[0] = new File(filename);
      }
      if (currFile[0] == null || !currFile[0].exists()) {
        Files.walkFileTree(rootFolder.toPath(), new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            currFile[0] = file.toFile();
            return FileVisitResult.TERMINATE;
          }
        });
      }
      return currFile[0];
    }
    catch (IOException ioe) {
      LOG.error("Cannot read current file", ioe);
      return null;
    }
  }

  public void setCurrentFile(File f) {
    try {
      File indexFile = new File(rootFolder, "_current");
      Files.writeString(indexFile.toPath(), f.getAbsolutePath());
    }
    catch (IOException ioe) {
      LOG.error("Cannot save current file", ioe);
    }
  }  
  
  @FXML
  private void onPrevImage() {
    onAutosave(vpsTable.get(), () -> {
      try {
        Path imagePath = imageFile.get().toPath();
        Path[] ret = { null, null };
        Files.walkFileTree(rootFolder.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
              if (imagePath.equals(file)) {
                ret[1] = file;
                return ret[0] != null? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
              }
              ret[0] = file;  
              return FileVisitResult.CONTINUE;
            }
        });
        setFile(ret[0].toFile());
      }
      catch (IOException ioe) {
        LOG.error("cannot find previous image", ioe);
      }
    });
  }

  @FXML
  private void onNextImage() {
    onAutosave(vpsTable.get(), () -> {
      try {
        Path imagePath = imageFile.get().toPath();
        Path[] ret = { null, null };
        Files.walkFileTree(rootFolder.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
              if (imagePath.equals(ret[0])) {
                ret[1] = file;
                return FileVisitResult.TERMINATE;
              }
              ret[0] = file;
              // also memorize first file in case
              if (ret[1] == null) {
                ret[1] = file;
              }
              return FileVisitResult.CONTINUE;
            }
        });
        setFile(ret[1].toFile());
      }
      catch (IOException ioe) {
        LOG.error("cannot find previous image", ioe);
      }
    });
  }

  private void onAutosave(VpsTable table, @Nullable Runnable onSuccess) {
    if (table!=null) {
      if (autosaveCheckbox.isSelected()) {
        if (!save(table)) {
          return;
        }
      }
      else if (dirty.get()) {
        Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Data has been changed, do you want to save it ?");
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
          if (!save(table)) {
            return;
          }
        }
        else {
          // click cancel, stay on the table
          return;
        }
      }
    }
    if (onSuccess != null) {
      onSuccess.run();
    }
  }

  @FXML
  private void onSave() {
    VpsTable table = vpsTable.get();
    if (table!=null) {
      save(table);
    }
  }

  //------------------------------------------

  private String getImageBase64(File file) throws IOException {
    byte[] imageBytes = Files.readAllBytes(file.toPath());
    return Base64.getEncoder().encodeToString(imageBytes);
  }

  private VpsTableData loadJson(VpsTable table) throws IOException {
    File jsonFile = new File(database, table.getId() + ".json");
    // Read existing data
    ObjectMapper objectMapper = new ObjectMapper();
    VpsTableData data = null;
    if (jsonFile.exists()) {
      data = objectMapper.readValue(jsonFile, VpsTableData.class);  
    }
    else {
      data = new VpsTableData();
      data.setId(table.getId());
      data.setDisplayName(table.getDisplayName());
    }
    return data;
  }

  public boolean load(VpsTable table) {
    try {
      VpsTableData data = loadJson(table);

      // everything ok, set the table
      vpsTable.set(table);

      // get existings instructions for the image
      if (imageFile.get() != null) {
        String imageBase64 = getImageBase64(imageFile.get());
        VpsTableInstructions instructionSet = data.getInstructionSetFor(imageBase64);

        languagesCombo.setValue(StringUtils.defaultString(instructionSet.getLanguage(), "EN"));
        String[] instructions = instructionSet.getInstructions();
        if (instructions != null && instructions.length > 0) {
          String instructionsText = StringUtils.join(instructions, System.lineSeparator()+System.lineSeparator());
          instructionsTextArea.setText(instructionsText);
        }
      }

      dirty.set(false);
      return true;
    }
    catch (IOException ioe) {
      LOG.error("Cannot load table " + table.getDisplayName(), ioe);
      return false;
    }
  }

  private void saveJson(VpsTable table, VpsTableData data) throws IOException {
    File jsonFile = new File(database, table.getId() + ".json");
    // Read existing data
    ObjectMapper objectMapper = new ObjectMapper();

    DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("  ", DefaultIndenter.SYS_LF);
    DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
    printer.indentObjectsWith(indenter);
    printer.indentArraysWith(indenter);

    // Serialize it using the custom printer
    objectMapper.writer(printer).writeValue(jsonFile, data);  
  }

  public boolean save(VpsTable table) {
    try {
      VpsTableData data = loadJson(table);
      // now modify the data with the modification
      data.setId(table.getId());
      data.setDisplayName(table.getDisplayName());

      if (imageFile.get() != null) {
        // get or create for the image
        String base64 = getImageBase64(imageFile.get());
        VpsTableInstructions instructionSet = data.getInstructionSetFor(base64);

        String language = StringUtils.defaultString(languagesCombo.getValue(), "EN");
        instructionSet.setLanguage(language);
        String[] instructions = instructionsTextArea.getText().split("\\R\\s*\\R");
        for (int i = 0; i < instructions.length; i++) {
          instructions[i] = instructions[i].trim();
        } 
        instructionSet.setInstructions(instructions);
      }

      // and save them
      saveJson(table, data);

      dirty.set(false);
      return true;
    }
    catch (IOException ioe) {
      LOG.error("Cannot save table " + table.getDisplayName(), ioe);
      return false;
    }
  }

  @FXML
  private void onVpsTableOpen() {
    if (vpsTable.get() != null) {
      Studio.browse(VPS.getVpsTableUrl(vpsTable.get().getId()));
    }
  }


  //---------------------------

  protected void unselectArea() {
    if (resizer != null) {
      resizer.removeFromPane(imagepane);
      resizer = null;
    }  
  }

  private BufferedImage getImage() {
    BufferedImage img = SwingFXUtils.fromFXImage(imageview.getImage(), null);
    if (resizer != null) {
      double sx = img.getWidth() / imageview.getFitWidth();
      double sy = img.getHeight() / imageview.getFitHeight();
      double s = Math.max(sx, sy);
      img = img.getSubimage((int) (s * resizer.getX()), (int) (s * resizer.getY()), 
                  (int) (s * resizer.getWidth()), (int) (s * resizer.getHeight()));
    }
    return img;
  }

  protected Tesseract getTesseract() {
    Tesseract instance = new Tesseract();
    instance.setDatapath(TESSERACT_FOLDER);
    instance.setLanguage("eng");
    //instance.setPageSegMode(1);
    return instance;
  }

  protected String extractText(BufferedImage img) {
    try {
      Tesseract tesseract = getTesseract();
      return  tesseract.doOCR(img);
    }
    catch (TesseractException te) {
      LOG.error("error in parsing image", te);
      return null;
    }
  }
}
