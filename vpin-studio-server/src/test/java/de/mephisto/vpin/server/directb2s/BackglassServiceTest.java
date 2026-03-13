package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.server.emulators.EmulatorService;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StreamUtils;

import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2SDetail;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.validation.BackglassValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;

@SpringBootTest
public class BackglassServiceTest extends AbstractVPinServerTest {

  @Autowired
  protected BackglassService backglassService;
  
  @Autowired
  protected BackglassValidationService backglassValidationService;

  @Autowired
  private EmulatorService emulatorService;

  @Test
  public void testGetBackglasses() {
    List<DirectB2S> b2s = backglassService.getBackglasses();
    assertEquals(5, b2s.size());

    DirectB2S b2s1 = b2s.get(0);
    assertEquals("250 cc (Inder 1992)" + File.separatorChar + "250 cc (Inder 1992).directb2s", b2s1.getFileName());
    assertEquals(1, b2s1.getVersions().size());

    DirectB2S b2s2 = b2s.get(1);
    assertEquals("Baseball (1970).directb2s", b2s2.getFileName());
    assertEquals(1, b2s2.getVersions().size());

    DirectB2S b2s3 = b2s.get(4);
    assertEquals("Twister (1996).directb2s", b2s3.getFileName());
    assertEquals(2, b2s3.getVersions().size());
  }

  @Test
  public void testGetBackglass() {
    Game g = gameService.getGameByBaseFilename(1, "Twister (1996)");
    assertNotNull(g);
    DirectB2S b2s = backglassService.getDirectB2SAndVersions(g);
    assertEquals("Twister (1996).directb2s", b2s.getFileName());
    assertEquals(2, b2s.getVersions().size());

    DirectB2SData data = backglassService.getDirectB2SData(g);
    assertNotNull(data);
    assertEquals(3, data.getTableType());
    assertEquals("STAT, Wildman, bassgeige", data.getAuthor());

    data = backglassService.getDirectB2SData(1, "Twister (1996) (1).directb2s");
    assertNotNull(data);
    assertEquals(2, data.getTableType());
    assertEquals("RetroBASH", data.getAuthor());

    DirectB2STableSettings settings = backglassService.getTableSettings(g);
    assertNotNull(settings);
    assertEquals(2, settings.getHideGrill());

    //-------------

    gameService.getImportableTables(1);

    b2s = backglassService.getDirectB2SAndVersions(1, "250 cc (Inder 1992)" + File.separatorChar + "250 cc (Inder 1992).directb2s");
    assertEquals("250 cc (Inder 1992)" + File.separatorChar + "250 cc (Inder 1992).directb2s", b2s.getFileName());
  }

  @Test
  public void testOperations() throws Exception {
    // check no folder
    doAllTests("Twister (1996)", 2);
    // check folder
    doAllTests("250 cc (Inder 1992)" + File.separatorChar + "250 cc (Inder 1992)", 1);
  }

  public void doAllTests(String directb2s, int nbVersions) throws Exception {
    DirectB2S b2s = backglassService.getDirectB2SAndVersions(1, directb2s + ".directb2s");
    assertEquals(directb2s + ".directb2s", b2s.getFileName());
    assertEquals(nbVersions, b2s.getVersions().size());
    assertTrue(b2s.isEnabled());

    GameEmulator emu = emulatorService.getGameEmulator(1);
    String f = b2s.getVersion(0);
    File file = new File(emu.getGamesDirectory(), f);
    assertTrue(file.exists());
    long size = Files.size(Path.of(emu.getGamesDirectory(), f));

    // Duplicate first version
    DirectB2S b2stest = backglassService.duplicate(emu.getId(), f);
    assertNotNull(b2stest);

    assertEquals(nbVersions + 1, b2stest.getVersions().size());
    for (int i = 0; i < nbVersions; i++) {
      assertEquals(b2s.getVersion(i), b2stest.getVersion(i));
    }
    // get the latest file, ie the added one
    String newF = b2stest.getVersion(nbVersions);

    // add or change fullDMD
    try (InputStream is = getClass().getResourceAsStream("fond fulldmd vert.jpg")) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      StreamUtils.copy(is, baos);
      String base64 = DatatypeConverter.printBase64Binary(baos.toByteArray());
      backglassService.setDmdImage(emu.getId(), newF, "fond fulldmd vert.jpg", base64);
    }
    long newSize = Files.size(Path.of(emu.getGamesDirectory(), newF));
    assertNotEquals(size, newSize);

    // set as default, flipping f and newf files 
    b2stest = backglassService.setAsDefault(emu.getId(), newF);
    assertEquals(nbVersions + 1, b2stest.getVersions().size());
    // check sizes
    assertEquals(newSize, Files.size(Path.of(emu.getGamesDirectory(), f)));
    assertEquals(size, Files.size(Path.of(emu.getGamesDirectory(), newF)));

    // Disable the backglasses, moving the duplicated file at the end
    b2stest = backglassService.disable(emu.getId(), f);
    assertEquals(nbVersions + 1, b2stest.getVersions().size());
    assertFalse(b2stest.isEnabled());
    for (int i = 0; i < nbVersions + 1; i++) {
      assertNotEquals(f, b2stest.getVersion(i));
    }

    // our orignal file being now named newF, set it as default, this should enable the backglass and rename f
    b2stest = backglassService.setAsDefault(emu.getId(), newF);
    assertTrue(b2stest.isEnabled());
    assertEquals(size, Files.size(Path.of(emu.getGamesDirectory(), f)));
    assertEquals(directb2s + ".directb2s", b2stest.getVersion(0));

    // delete the last file, ie our duplicated ones
    String newF2 = b2stest.getVersion(nbVersions);
    assertEquals(newSize, Files.size(Path.of(emu.getGamesDirectory(), newF2)));
    b2stest = backglassService.deleteVersion(emu.getId(), newF2);

    // control everything is back to initial situation
    assertEquals(b2s, b2stest);
    assertTrue(b2stest.isEnabled());
    assertEquals(directb2s + ".directb2s", b2stest.getFileName());
    assertEquals(directb2s + ".directb2s", b2stest.getVersion(0));
    assertEquals(nbVersions, b2stest.getVersions().size());
    for (int i = 0; i < nbVersions; i++) {
      assertEquals(b2s.getVersion(i), b2stest.getVersion(i));
    }
  
    // also verify we did not corrupt the cache
    List<DirectB2S> allb2s = backglassService.getBackglasses();
    assertEquals(5, allb2s.size());
  }

  @Test
  public void testBackglassValidations() throws Exception {

    Game g = gameService.getGameByBaseFilename(1, "Twister (1996)");
    DirectB2SDetail detail = backglassService.getBackglassDetail(1, "Twister (1996).directb2s", g);
    List<ValidationState> validations = detail.getValidations();
    assertEquals(1, validations.size());
    assertEquals(0, validations.get(0).getCode());

    detail = backglassService.getBackglassDetail(1, "Counterforce (Gottlieb 1980).directb2s", g);
    validations = detail.getValidations();
    assertEquals(1, validations.size());
    assertEquals(BackglassValidationCode.CODE_NO_GAME, validations.get(0).getCode());
   
    // rerun but get all
    DirectB2ServerSettings serverSettings = backglassService.getServerSettings();
    DirectB2STableSettings tableSettings = backglassService.getTableSettings(g);
    validations = backglassValidationService.validate(detail, g, tableSettings, serverSettings, false);
    assertEquals(2, validations.size());


    detail = backglassService.getBackglassDetail(1, "Jaws.directb2s", g);
    validations = detail.getValidations();
    assertEquals(1, validations.size());
    assertEquals(BackglassValidationCode.CODE_NO_FULLDMD, validations.get(0).getCode());
    tableSettings = backglassService.getTableSettings(g);
    validations = backglassValidationService.validate(detail, g, tableSettings, serverSettings, false);
    assertEquals(1, validations.size());
  }

  @Test
  public void testFullDMDManipulations() throws Exception {
    String b2sFilename = "Jaws.directb2s";
    
    //Game g = gameService.getGameByBaseFilename(1, );

    // first check backglass does not contain a full dmd
    String base64 = backglassService.getDmdBase64(1, b2sFilename);
    assertNull(base64);

    // upload a dummy image
    String dummyBase64 = "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";
    backglassService.setDmdImage(1, b2sFilename, "backglass_test.png", dummyBase64);

    // re-extract and compare
    base64 = backglassService.getDmdBase64(1, b2sFilename);
    assertEquals(dummyBase64, base64);

    // empty full dmd
    backglassService.setDmdImage(1, b2sFilename, null, null);
    base64 = backglassService.getDmdBase64(1, b2sFilename);
    assertNull(base64);
  }

}
