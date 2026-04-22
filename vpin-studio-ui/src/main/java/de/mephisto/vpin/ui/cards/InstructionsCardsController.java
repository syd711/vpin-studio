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
import java.util.ArrayList;
import java.util.Arrays;
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
import com.fasterxml.jackson.databind.SerializationFeature;

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
import de.mephisto.vpin.ui.util.AutoCompleteMatcher;
import de.mephisto.vpin.ui.util.AutoMatchModel;
import de.mephisto.vpin.ui.util.PositionSelection;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.beans.binding.Bindings;
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
import javafx.scene.control.IndexRange;
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
    private Label tableId;

    @FXML
    private ImageView imageView;

    @FXML
    private Pane imagepane;

    @FXML
    private Button prevBtn;

    @FXML
    private Button nextBtn;

    @FXML
    private TextArea instructionsTextArea;

    private ObjectProperty<VpsTable> vpsTable = new SimpleObjectProperty<>();

    private ObjectProperty<File> imageFile = new SimpleObjectProperty<>();

    private BooleanProperty dirty = new SimpleBooleanProperty(false);

    private PositionResizer resizer;

    private File rootFolder;
    private File database;
    private List<File> files = new ArrayList<>();
    private int index = 0;

    private final ObjectMapper objectMapper;

    public InstructionsCardsController() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @FXML
    private void onCancel() {
        Studio.stage.close();
    }

    @FXML
    private void onSave() {
        save();
    }

    @FXML
    private void onPrevious() {
        save();
        if (index > 0) {
            index--;
            refreshFile();
        }
    }

    @FXML
    private void onNext() {
        save();
        if (index < files.size() - 1) {
            index++;
            refreshFile();
        }
    }

    private void refreshFile() {
        if (index >= 0 && index < files.size()) {
            File file = files.get(index);
            setData(file);

            String name = file.getName();
            VpsTable table = VPS.getInstance().getTableByDisplayName(name);
            if (table == null) {
                table = VPS.getInstance().findTableByFilename(name);
            }
            vpsTable.set(table);
            if (table != null) {
                autoCompleteNameField.setText(table.getDisplayName());
            }
            else {
                autoCompleteNameField.setText("");
            }
        }
    }

    @FXML
    private void onOpenVpsTable() {
        Studio.browse(VPS.getVpsTableUrl(vpsTable.get().getId()));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        vpsTable.addListener((observable, oldValue, newValue) -> {
            openVpsTableBtn.setDisable(newValue == null);
            if (newValue != null) {
                instructionsTextArea.setText(getInstructions(newValue.getId()));
                dirty.set(false);
            }
            else {
                instructionsTextArea.setText("");
            }
        });

        autoCompleteNameField = new AutoCompleteTextField(tableNameField, (value) -> {
            VpsTable table = VPS.getInstance().getTableByDisplayName(value);
            vpsTable.set(table);
        }, new AutoCompleteMatcher() {
            @Override
            public List<AutoMatchModel> match(String searchTerm) {
                List<VpsTable> matches = VPS.getInstance().find(searchTerm);
                return matches.stream().map(e -> new AutoMatchModel(e.getDisplayName(), e.getDisplayName())).collect(Collectors.toList());
            }
        });

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
        tableId.textProperty().bind(Bindings.createStringBinding(() -> vpsTable.get() == null ? "" : vpsTable.get().getId(), vpsTable));

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

                    Tesseract tesseract = new Tesseract();
                    tesseract.setDatapath(TESSERACT_FOLDER);
                    try {
                        BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                        BufferedImage cropped = image.getSubimage(resizer.getX(), resizer.getY(), resizer.getWidth(), resizer.getHeight());
                        String text = tesseract.doOCR(cropped);
                        if (StringUtils.isNotEmpty(text)) {
                            instructionsTextArea.setText(instructionsTextArea.getText() + " " + text.trim());
                        }
                    } catch (TesseractException e) {
                        LOG.error("Tesseract error: " + e.getMessage(), e);
                        WidgetFactory.showAlert(Studio.stage, "Error", "OCR error: " + e.getMessage());
                    } catch (Exception e) {
                        LOG.error("OCR error: " + e.getMessage(), e);
                        WidgetFactory.showAlert(Studio.stage, "Error", "OCR error: " + e.getMessage());
                    }
                });
    }

    private void unselectArea() {
        resizer = null;
    }

    private void createTransformationButton(String label, Function<String, String> transformation) {
        Button btn = new Button(label);
        btn.setOnAction(e -> {
            String text = instructionsTextArea.getSelectedText();
            if (StringUtils.isEmpty(text)) {
                text = instructionsTextArea.getText();
                instructionsTextArea.setText(transformation.apply(text));
            }
            else {
                IndexRange selection = instructionsTextArea.getSelection();
                String transformed = transformation.apply(text);
                instructionsTextArea.replaceText(selection, transformed);
            }
        });
        toolbar.getItems().add(btn);
    }

    private String onLowerCase(String txt) {
        return txt.toLowerCase();
    }

    private String onRemoveLineBreaks(String txt) {
        StringTokenizer st = new StringTokenizer(txt, "\n");
        StringBuilder sb = new StringBuilder();
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken().trim());
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    private void save() {
        if (vpsTable.get() != null) {
            setInstructions(vpsTable.get().getId(), instructionsTextArea.getText());
            dirty.set(false);
        }
    }

    private String getInstructions(String id) {
        if (database == null) return "";
        File jsonFile = new File(database, id + ".json");
        if (jsonFile.exists()) {
            try {
                VpsTableData data = objectMapper.readValue(jsonFile, VpsTableData.class);
                if (!data.getInstructionSets().isEmpty()) {
                    return String.join("\n", data.getInstructionSets().get(0).getInstructions());
                }
            } catch (IOException e) {
                LOG.error("Failed to read instructions: " + e.getMessage(), e);
            }
        }
        return "";
    }

    private void setInstructions(String id, String text) {
        if (database == null) return;
        File jsonFile = new File(database, id + ".json");
        VpsTableData data;
        if (jsonFile.exists()) {
            try {
                data = objectMapper.readValue(jsonFile, VpsTableData.class);
            } catch (IOException e) {
                LOG.error("Failed to read instructions: " + e.getMessage(), e);
                data = new VpsTableData();
            }
        } else {
            data = new VpsTableData();
        }
        data.setId(id);
        data.setDisplayName(vpsTable.get().getDisplayName());

        VpsTableInstructions instructions;
        if (data.getInstructionSets().isEmpty()) {
            instructions = new VpsTableInstructions();
            data.getInstructionSets().add(instructions);
        } else {
            instructions = data.getInstructionSets().get(0);
        }
        instructions.setId(id);
        instructions.setInstructions(text.split("\n"));

        try {
            objectMapper.writeValue(jsonFile, data);
        } catch (IOException e) {
            LOG.error("Failed to save instructions: " + e.getMessage(), e);
        }
    }

    public void setData(File file) {
        this.imageFile.set(file);
        if (file != null) {
            try (InputStream in = new FileInputStream(file)) {
                Image image = new Image(in);
                imageView.setImage(image);
            } catch (IOException e) {
                LOG.error("Failed to load image: " + e.getMessage(), e);
            }
        }
    }

    public void setRootFolder(File folder) {
        this.rootFolder = folder;
        File[] listFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));
        if (listFiles != null) {
            for (File file : listFiles) {
                files.add(file);
            }
        }
        refreshFile();
    }

    public void setDatabase(File database) {
        this.database = database;
    }
}