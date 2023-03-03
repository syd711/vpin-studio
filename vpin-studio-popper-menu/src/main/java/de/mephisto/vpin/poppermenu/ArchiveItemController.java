package de.mephisto.vpin.poppermenu;

import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;

public class ArchiveItemController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ArchiveItemController.class);

  @FXML
  private ImageView imageView;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void setData(VpaDescriptorRepresentation descriptor) {
    String thumbnail = descriptor.getManifest().getThumbnail();
    if (thumbnail == null) {
      Image wheel = new Image(MenuMain.class.getResourceAsStream("avatar-blank.png"));
      imageView.setImage(wheel);
      return;
    }

    byte[] decode = Base64.getDecoder().decode(thumbnail);
    Image wheel = new Image(new ByteArrayInputStream(decode));
    imageView.setImage(wheel);
  }
}
