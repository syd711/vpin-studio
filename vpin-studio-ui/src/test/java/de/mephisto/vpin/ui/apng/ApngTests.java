package de.mephisto.vpin.ui.apng;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import de.mephisto.vpin.ui.apng.image.ApngFrame;
import de.mephisto.vpin.ui.apng.image.ApngFrameDecoder;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs a serie of APNG tests
 * @see https://philip.html5.org/tests/apng/tests.html
 */
public class ApngTests extends Application {

  static {
    ApngImageLoaderFactory.install();
  }

  private LinkedHashMap<String, String> getTests() throws IOException {
    LinkedHashMap<String, String> tests = new LinkedHashMap<>();
    try (InputStream in = getClass().getResourceAsStream("apng_tests.txt")) {
      List<String> lines = IOUtils.readLines(in, StandardCharsets.UTF_8);
      for (String line : lines) {
        if (StringUtils.isNotBlank(line)) {
          String imgfile = StringUtils.substringBefore(line, "#").trim();
          String text = StringUtils.substringAfter(line, "#").trim();
          tests.put(imgfile, text);
        }
      }
    }
    return tests;
  }

  private Map.Entry<String, String> findTest(LinkedHashMap<String, String> tests, int i) {
    Iterator<Map.Entry<String, String>> iter = tests.entrySet().iterator();
    Map.Entry<String, String> test = null;
    int n = 0;
    while (iter.hasNext() && n++ <= i) {
      test = iter.next();
    }
    return test;
  }

  //----------------------------------------------

  @Test
  public void testOneImage() throws Exception {
    // pick one test (starting by 1)
    int testIdx = 13;

    LinkedHashMap<String, String> tests = getTests();
    Map.Entry<String, String> test = findTest(tests, testIdx);
    runtest(test);
  }

  @Test
  public void testImages() throws Exception {
    LinkedHashMap<String, String> tests = getTests();
    for (Map.Entry<String, String> test: tests.entrySet()) {
      runtest(test);
    }
  }

  private void runtest(Map.Entry<String, String> test) {
    String imgfile = test.getKey();
    boolean isFailing = false;
    if (imgfile.startsWith("x ")) {
      isFailing = true;
      imgfile = imgfile.substring(2);
    }
    System.out.println("Running test " + imgfile + ": " + test.getValue());
    try (InputStream img = getClass().getResourceAsStream(imgfile);
          ApngFrameDecoder decoder = new ApngFrameDecoder(img)) {
      // process all frames
      ApngFrame frame;
      int nbFramesDecoded = 0;
      while ((frame = decoder.nextFrame()) != null) {
        nbFramesDecoded++;
        assertEquals(128, frame.getWidth());
        assertEquals(64, frame.getHeight());
      }
      
      // if no ACTL chunk in the image, getNbAvailableFrames() return -1 and it is normal
      int availFrames = decoder.getNbAvailableFrames();
      if (availFrames > 0) {
        assertEquals(availFrames, nbFramesDecoded);
      }

      if (isFailing) {
        // should fail but didn't
        fail("Image " + imgfile + " should have raised an exception : " + test.getValue());
      }
    }
    catch (IOException e) {
      if (isFailing) {
        // as expected
      }
      else {
        fail("Image " + imgfile + " shouldn't raise an exception : " + test.getValue(), e);
      }
    }
  }

  //------------------------------------------

  @Override
  public void start(final Stage stage) throws Exception {
    BorderPane layout = new BorderPane();
    stage.setScene(new Scene(layout, 500, 500));

    Label label = new Label();
    Font font = Font.font("Verdana", FontWeight.BOLD, 12);
    label.setFont(font);
    label.setTextFill(Color.WHITE);
    label.setWrapText(true);
    HBox topbar = new HBox(label);
    topbar.setPadding(new Insets(5));
    layout.setTop(topbar); 

    ImageView iv = new ImageView();

    HBox pane = new HBox(iv);
    pane.setBackground(Background.fill(Color.WHITE));
    pane.setPadding(new Insets(10));
    layout.setCenter(pane);

    // First test to start with
    int[] currentTest = { 0 };

    LinkedHashMap<String, String> tests = getTests();
    loadTest(iv, label, tests, currentTest[0]);

    Button prevTest = new Button("Previous Test");
    prevTest.setOnAction(e -> {
      if (currentTest[0] > 0) {
        currentTest[0] --;
        loadTest(iv, label, tests, currentTest[0]);
      }
    });
    Button nextTest = new Button("Next Test");
    nextTest.setOnAction(e -> {
      if (currentTest[0] < tests.size() - 1) {
        currentTest[0] ++;
        loadTest(iv, label, tests, currentTest[0]);
      }
    });

    HBox toolbar = new HBox(prevTest, nextTest);
    toolbar.setPadding(new Insets(15));
    toolbar.setSpacing(20);
    layout.setBottom(toolbar);

    stage.show();
  }

  private void loadTest(ImageView iv, Label label, LinkedHashMap<String, String> tests, int i) {
    Map.Entry<String, String> test = findTest(tests, i);
    if (test != null) {
      String imgfile = test.getKey();
      boolean isFailing = false;
      if (imgfile.startsWith("x ")) {
        isFailing = true;
        imgfile = imgfile.substring(2);
      }

      try (InputStream imgStream = getClass().getResourceAsStream(imgfile)) {
        Image image = new Image(imgStream);
        iv.setImage(image);
      }
      catch (Exception e) {
      }

      label.setText(imgfile  + ": " + test.getValue());
      if (isFailing) {
        ((HBox) label.getParent()).setBackground(Background.fill(Color.RED));
      } else {
        ((HBox) label.getParent()).setBackground(Background.fill(Color.GREEN));
      }
    }
  }

  public static void main(String[] args) {
    Application.launch(args);
  }
}