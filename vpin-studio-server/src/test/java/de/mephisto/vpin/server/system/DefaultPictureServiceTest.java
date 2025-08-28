package de.mephisto.vpin.server.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

public class DefaultPictureServiceTest {

  @Test
  public void testManufacturerPicture() {
    SystemService.RESOURCES = "../resources/";
    DefaultPictureService svc = new DefaultPictureService();

    File f;

    f = svc.getManufacturerPicture("CapCom", 2000, true);
    assertEquals("Capcom (1989-).png", f.getName());

    f = svc.getManufacturerPicture("CapCom", 1988, true);
    assertEquals("Capcom(1985-1988).png", f.getName());

        f = svc.getManufacturerPicture("CapCom", 1988, false);
    assertEquals("Capcom.png", f.getName());

    f = svc.getManufacturerPicture("CapCom", 1960, true);
    assertEquals("Capcom.png", f.getName());

    f = svc.getManufacturerPicture("midway", 2000, true);
    assertEquals("Midway (1997-).png", f.getName());

    f = svc.getManufacturerPicture("midway", 1997, true);
    assertEquals("Midway (1997-).png", f.getName());
    
    f = svc.getManufacturerPicture("data east", 1978, true);
    assertEquals("Data East.png", f.getName());

    f = svc.getManufacturerPicture("data east", 1978, false);
    assertEquals("Data East.png", f.getName());

    f = svc.getManufacturerPicture("Bally", 1985, true);
    assertEquals("Bally.png", f.getName());

  }

}
