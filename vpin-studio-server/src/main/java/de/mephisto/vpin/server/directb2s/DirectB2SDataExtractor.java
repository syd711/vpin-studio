package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2SDataScore;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

public class DirectB2SDataExtractor extends DefaultHandler {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SDataExtractor.class);

  private DirectB2SData data;

  private String backgroundBase64;
  private String dmdBase64;

  public DirectB2SDataExtractor() {
  }

  public DirectB2SData extractData(@NonNull File directB2S, int emulatorId, String filename) {
    this.data = new DirectB2SData();
    this.data.setFilename(filename);
    this.data.setFilesize(directB2S.length());
    this.data.setEmulatorId(emulatorId);
    this.data.setModificationDate(new Date(directB2S.lastModified()));
    if (directB2S.exists()) {
      try (InputStream in = new FileInputStream(directB2S)) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(in, this);
      } catch (Exception e) {
        String msg = "Failed to parse directB2S '" + directB2S.getAbsolutePath() + "': " + e.getMessage();
        LOG.error(msg);
      }
    }
    return data;
  }

  public String getName() {
    return data!=null? data.getName(): null;
  }

  public String getBackgroundBase64() {
    return backgroundBase64;
  }
  public String getDmdBase64() {
    return dmdBase64;
  }

  @Override
  public void startElement(String uri, String lName, String qName, Attributes attr) {
    switch (qName) {
      case "Name": {
        data.setName(attr.getValue("Value").trim());
        break;
      }
      case "TableType": {
        String value = attr.getValue("Value");
        if (!StringUtils.isEmpty(value)) {
          data.setTableType(Integer.parseInt(value));
        }
        break;
      }
      case "DualBackglass": {
        String value = attr.getValue("Value");
        data.setDualBackglass(Integer.parseInt(value));
        break;
      }
      case "B2SDataCount": {
        String value = attr.getValue("Value");
        if (!StringUtils.isEmpty(value)) {
          data.setB2sElements(Integer.parseInt(value));
        }
        break;
      }
      case "GrillHeight": {
        String value = attr.getValue("Value");
        if (!StringUtils.isEmpty(value)) {
          data.setGrillHeight(Integer.parseInt(value));
        }
        /*String smallGrillHeight = attr.getValue("Small");
        if (!StringUtils.isEmpty(smallGrillHeight)) {
          data.setSmallGrillHeight(Integer.parseInt(smallGrillHeight));
        }*/
        break;
      }
      case "Author": {
        data.setAuthor(String.valueOf(attr.getValue("Value").trim()));
        break;
      }
      case "Artwork": {
        data.setArtwork(String.valueOf(attr.getValue("Value").trim()));
        break;
      }
      case "NumberOfPlayers": {
        String value = attr.getValue("Value");
        if (!StringUtils.isEmpty(value)) {
          data.setNumberOfPlayers(Integer.parseInt(value));
        }
        break;
      }
      case "Score": {
        //<Score Parent="DMD" ID="1" ReelType="Dream7LED8" ReelLitColor="255.0.0" ReelDarkColor="15.15.15" 
        //    Glow="1500" Thickness="1000" Shear="6" Digits="7" Spacing="25" DisplayState="0" 
        //    LocX="58" LocY="568" Width="585" Height="70" />
        DirectB2SDataScore score = new DirectB2SDataScore();
        score.setId(attr.getValue("ID"));
        score.setParent(attr.getValue("Parent"));
        score.setX(safeGetInt(attr, "LocX", -1));
        score.setY(safeGetInt(attr, "LocY", -1));
        score.setWidth(safeGetInt(attr, "Width", 0));
        score.setHeight(safeGetInt(attr, "Height", 0));
        score.setNbDigits(safeGetInt(attr, "Digits", 0));
        // O means ON, 1 means OFF
        score.setDisplayState(safeGetInt(attr, "DisplayState", 0));
        data.addScore(score);
        break;
      }
      case "Bulb": {
        data.setIlluminations(data.getIlluminations() + 1);
        break;
      }
      case "BackglassImage": {
        //data.setBackgroundBase64(attr.getValue("Value"));
        backgroundBase64 = attr.getValue("Value");
        data.setBackgroundAvailable(true);
        break;
      }
      case "DMDImage": {
        //data.setDmdBase64(attr.getValue("Value"));
        dmdBase64 = attr.getValue("Value");
        data.setDmdImageAvailable(true);
        break;
      }

/*
Other elements in XML :  path / from / top (interesting attributes)

      Images / BackglassOffImage (Value)    [alternative to BackglassImage]
      Images / BackglassOnImage (Value)

      DMDType : B2SData.eDMDType.B2SAlwaysOnSecondMonitor, 
        B2SData.eDMDType.B2SAlwaysOnThirdMonitor, B2SData.eDMDType.B2SOnSecondOrThirdMonitor

      Reels / Image
      Reels / Images / Image
      Reels / IlluminatedImages 
      Reels / IlluminatedImages/Set

      Sounds / Sound (name, stream)

      Animations/Animation
*/
    }
  }

  private int safeGetInt(Attributes attr, String name, int defaultValue) {
    String v = attr.getValue(name);
    try {
      return StringUtils.isNotEmpty(v) ? Integer.parseInt(v) : defaultValue;
    } 
    catch (Exception e) {
      LOG.warn("Cannot parse integer attribute " + name + ", value " + v + ", ignored error " + e.getMessage());
      return defaultValue;
    }
  }

}
