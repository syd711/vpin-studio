package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that .B2SPro files, a superset of the .directb2s format, keep all their elements and attributes
 * when they are updated.
 */
public class DirectB2SDataUpdaterTest {

  private static final String B2SPRO = """
      <DirectB2SData Version="1.27">
        <Name Value="300 (Gottlieb 1975)" />
        <TableType Value="1" />
        <DMDType Value="0" />
        <ProjectGUID2 Value="4AE45A45-D5C5-4F07-B450-8A505B3291EF" />
        <Author Value="wildman" />
        <NumberOfPlayers Value="4" />
        <ReelColor Value="255.69.0" />
        <Animations />
        <Scores ReelCountOfIntermediates="0" ReelRollingDirection="Up" ReelRollingInterval="20">
          <Score Parent="Backglass" ID="1" ReelType="ImportedEMR_T1_0" Reel3DEnabled="1" Reel3DDepth="120" Digits="5" DisplayState="0" LocX="671" LocY="410" Width="813" Height="137" />
          <Score Parent="Backglass" ID="2" ReelType="ImportedEMR_T1_0" Reel3DEnabled="1" Reel3DDepth="120" Digits="5" DisplayState="1" LocX="1663" LocY="412" Width="813" Height="137" />
        </Scores>
        <Reels>
          <Images>
            <Image Name="ImportedEMR_T1_0" CountOfIntermediates="0" Image="iVBORw0KGgo=" />
          </Images>
          <IlluminatedImages>
            <Set ID="1">
              <IlluminatedImage Name="ImportedEMR_T1_0" CountOfIntermediates="0" Image="iVBORw0KGgo=" />
            </Set>
          </IlluminatedImages>
        </Reels>
        <Illumination>
          <Bulb Parent="Backglass" ID="329" Name="" B2SID="33" LightRotationAngle="0.216209456" LightDesignLocX="1984" ArtworkPixelLighting="0" GlobalMaskLayerExplicit="0" Image="iVBORw0KGgo=" />
        </Illumination>
        <Images B2SProTransparentCanvasBacking="96.96.96">
          <BackglassImage Value="iVBORw0KGgo=" />
          <ThumbnailImage Value="iVBORw0KGgo=" />
        </Images>
        <B2SProDesignerData Encoding="base64-utf8">PEIyU0JhY2tnbGFzc0RhdGEgVmVyc2lvbj0iMS4yNyI+</B2SProDesignerData>
      </DirectB2SData>
      """;

  @TempDir
  Path tempDir;

  @Test
  void scoresUpdateKeepsUnknownB2SProElements() throws Exception {
    File b2sPro = createB2SPro();
    Document original = parse(b2sPro);

    DirectB2SDataUpdater updater = new DirectB2SDataUpdater();
    updater.upddateScoresDisplayState(b2sPro, true, false);

    Document updated = parse(b2sPro);
    // only the score display state has changed
    List<String> originalNodes = flatten(original, Set.of("DisplayState", "_DisplayState"));
    assertThat(flatten(updated, Set.of("DisplayState", "_DisplayState"))).isEqualTo(originalNodes);
    Element score = (Element) updated.getElementsByTagName("Score").item(0);
    assertThat(score.getAttribute("DisplayState")).isEqualTo("1");
    assertThat(score.getAttribute("_DisplayState")).isEqualTo("0");

    // restoring the scores gives the original document back
    updater.upddateScoresDisplayState(b2sPro, false, false);
    assertThat(flatten(parse(b2sPro), Set.of())).isEqualTo(flatten(original, Set.of()));
  }

  @Test
  void dmdImageUpdateKeepsUnknownB2SProElements() throws Exception {
    File b2sPro = createB2SPro();
    Document original = parse(b2sPro);

    DirectB2SDataUpdater updater = new DirectB2SDataUpdater();
    updater.updateDmdImage(b2sPro, "dmd.png", "iVBORw0KGgoAAAA=", false);

    Document updated = parse(b2sPro);
    NodeList dmdImages = updated.getElementsByTagName("DMDImage");
    assertThat(dmdImages.getLength()).isEqualTo(1);
    assertThat(dmdImages.item(0).getParentNode().getNodeName()).isEqualTo("Images");
    assertThat(((Element) dmdImages.item(0)).getAttribute("FileName")).isEqualTo("dmd.png");

    // removing the DMD image again gives the original document back, apart from the DMDType
    updater.updateDmdImage(b2sPro, null, null, false);
    assertThat(parse(b2sPro).getElementsByTagName("DMDImage").getLength()).isZero();
    assertThat(flatten(parse(b2sPro), Set.of("Value"))).isEqualTo(flatten(original, Set.of("Value")));
    assertThat(((Element) parse(b2sPro).getElementsByTagName("B2SProDesignerData").item(0)).getAttribute("Encoding")).isEqualTo("base64-utf8");
  }

  @Test
  void b2sProDataIsExtracted() throws Exception {
    File b2sPro = createB2SPro();
    DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
    DirectB2SData data = extractor.extractData(b2sPro, 1, b2sPro.getName());

    assertThat(data.getName()).isEqualTo("300 (Gottlieb 1975)");
    assertThat(data.getAuthor()).isEqualTo("wildman");
    assertThat(data.getNumberOfPlayers()).isEqualTo(4);
    assertThat(data.getScores()).hasSize(2);
    assertThat(data.getIlluminations()).isEqualTo(1);
    assertThat(data.isBackgroundAvailable()).isTrue();
  }

  private File createB2SPro() throws Exception {
    File file = tempDir.resolve("300 (Gottlieb 1975).B2SPro").toFile();
    Files.writeString(file.toPath(), B2SPRO, StandardCharsets.UTF_8);
    return file;
  }

  private static Document parse(File file) throws Exception {
    return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
  }

  /**
   * Flattens the document into one line per element: path, sorted attributes and trimmed text content.
   */
  private static List<String> flatten(Document document, Set<String> ignoredAttributes) {
    List<String> result = new ArrayList<>();
    flatten(document.getDocumentElement(), "", ignoredAttributes, result);
    return result;
  }

  private static void flatten(Element element, String parentPath, Set<String> ignoredAttributes, List<String> result) {
    String path = parentPath + "/" + element.getTagName();
    TreeMap<String, String> attributes = new TreeMap<>();
    NamedNodeMap attrs = element.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Node attr = attrs.item(i);
      if (!ignoredAttributes.contains(attr.getNodeName())) {
        attributes.put(attr.getNodeName(), attr.getNodeValue());
      }
    }
    StringBuilder text = new StringBuilder();
    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i).getNodeType() == Node.TEXT_NODE) {
        text.append(children.item(i).getNodeValue().trim());
      }
    }
    result.add(path + " " + attributes + " " + text);
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof Element child) {
        flatten(child, path, ignoredAttributes, result);
      }
    }
  }
}
