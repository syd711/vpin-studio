package de.mephisto.vpin.server.components;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.components.facades.*;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ComponentServiceTest {

  @Mock
  private ComponentRepository componentRepository;
  @Mock
  private SystemService systemService;
  @Mock
  private VPinMAMEComponent vPinMAMEComponent;
  @Mock
  private VpxComponent vpxComponent;
  @Mock
  private BackglassComponent backglassComponent;
  @Mock
  private FlexDMDComponent flexDMDComponent;
  @Mock
  private FreezyComponent freezyComponent;
  @Mock
  private SerumComponent serumComponent;
  @Mock
  private DOFLinxComponent dofLinxComponent;
  @Mock
  private DOFComponent dofComponent;

  @InjectMocks
  private ComponentService service;

  // ---- setVersion ----

  @Test
  void setVersion_returnsTrue_whenComponentFound() {
    Component component = componentWithType(ComponentType.vpinball);
    when(componentRepository.findByType(ComponentType.vpinball)).thenReturn(Optional.of(component));

    boolean result = service.setVersion(ComponentType.vpinball, "10.8.0");

    assertTrue(result);
    assertEquals("10.8.0", component.getInstalledVersion());
    verify(componentRepository).saveAndFlush(component);
  }

  @Test
  void setVersion_returnsFalse_whenComponentNotFound() {
    when(componentRepository.findByType(ComponentType.vpinball)).thenReturn(Optional.empty());

    assertFalse(service.setVersion(ComponentType.vpinball, "10.8.0"));
  }

  @Test
  void setVersion_setsNull_whenDashProvided() {
    Component component = componentWithType(ComponentType.vpinball);
    when(componentRepository.findByType(ComponentType.vpinball)).thenReturn(Optional.of(component));

    service.setVersion(ComponentType.vpinball, "-");

    assertNull(component.getInstalledVersion());
  }

  @Test
  void setVersion_setsNull_whenEmptyStringProvided() {
    Component component = componentWithType(ComponentType.vpinball);
    when(componentRepository.findByType(ComponentType.vpinball)).thenReturn(Optional.of(component));

    service.setVersion(ComponentType.vpinball, "");

    assertNull(component.getInstalledVersion());
  }

  // ---- setTargetFolder ----

  @Test
  void setTargetFolder_persistsOverride_whenComponentFound() {
    Component component = componentWithType(ComponentType.vpinmame);
    when(componentRepository.findByType(ComponentType.vpinmame)).thenReturn(Optional.of(component));

    boolean result = service.setTargetFolder(ComponentType.vpinmame, "C:/MTCPincab/VisualPinball/VPinMAME");

    assertTrue(result);
    assertEquals("C:/MTCPincab/VisualPinball/VPinMAME", component.getTargetFolder());
    verify(componentRepository).saveAndFlush(component);
  }

  @Test
  void setTargetFolder_clearsOverride_whenEmptyProvided() {
    Component component = componentWithType(ComponentType.vpinmame);
    component.setTargetFolder("C:/old/override");
    when(componentRepository.findByType(ComponentType.vpinmame)).thenReturn(Optional.of(component));

    service.setTargetFolder(ComponentType.vpinmame, "");

    assertNull(component.getTargetFolder());
  }

  @Test
  void setTargetFolder_returnsFalse_whenComponentNotFound() {
    when(componentRepository.findByType(ComponentType.vpinmame)).thenReturn(Optional.empty());

    assertFalse(service.setTargetFolder(ComponentType.vpinmame, "C:/somewhere"));
  }

  // ---- getEffectiveTargetFolder ----

  @Test
  void getEffectiveTargetFolder_prefersOverride_whenSet() {
    Component component = componentWithType(ComponentType.vpinmame);
    component.setTargetFolder("C:/MTCPincab/VisualPinball/VPinMAME");
    when(componentRepository.findByType(ComponentType.vpinmame)).thenReturn(Optional.of(component));

    File result = service.getEffectiveTargetFolder(ComponentType.vpinmame);

    assertEquals(new File("C:/MTCPincab/VisualPinball/VPinMAME"), result);
    verifyNoInteractions(vPinMAMEComponent);
  }

  @Test
  void getEffectiveTargetFolder_fallsBackToFacade_whenNoOverride() {
    Component component = componentWithType(ComponentType.vpinmame);
    when(componentRepository.findByType(ComponentType.vpinmame)).thenReturn(Optional.of(component));
    File autoDetected = new File("C:/vPinball/VisualPinball/VPinMAME");
    when(vPinMAMEComponent.getTargetFolder()).thenReturn(autoDetected);

    File result = service.getEffectiveTargetFolder(ComponentType.vpinmame);

    assertEquals(autoDetected, result);
  }

  // ---- ignoreVersion ----

  @Test
  void ignoreVersion_appendsVersion_whenNoExistingIgnored() throws IOException {
    Component component = componentWithType(ComponentType.freezy);
    component.setIgnoredVersions(null);
    when(componentRepository.findByType(ComponentType.freezy)).thenReturn(Optional.of(component));
    when(freezyComponent.loadReleases()).thenReturn(List.of());

    boolean result = service.ignoreVersion(ComponentType.freezy, "1.9.0");

    assertTrue(result);
    assertTrue(component.getIgnoredVersions().contains("1.9.0"));
  }

  @Test
  void ignoreVersion_deduplicates_whenVersionAlreadyPresent() throws IOException {
    Component component = componentWithType(ComponentType.freezy);
    component.setIgnoredVersions("1.9.0");
    when(componentRepository.findByType(ComponentType.freezy)).thenReturn(Optional.of(component));
    when(freezyComponent.loadReleases()).thenReturn(List.of());

    service.ignoreVersion(ComponentType.freezy, "1.9.0");

    long count = java.util.Arrays.stream(component.getIgnoredVersions().split(","))
        .filter("1.9.0"::equals).count();
    assertEquals(1, count);
  }

  @Test
  void ignoreVersion_returnsFalse_whenComponentNotFound() {
    when(componentRepository.findByType(ComponentType.freezy)).thenReturn(Optional.empty());

    assertFalse(service.ignoreVersion(ComponentType.freezy, "1.9.0"));
  }

  // ---- getReleases ----

  @Test
  void getReleases_returnsNull_whenCacheNotPopulated() {
    assertNull(service.getReleases(ComponentType.vpinball));
  }

  // ---- getComponentFacade ----

  @Test
  void getComponentFacade_returnsCorrectFacade_forEachType() {
    assertSame(vPinMAMEComponent, service.getComponentFacade(ComponentType.vpinmame));
    assertSame(vpxComponent, service.getComponentFacade(ComponentType.vpinball));
    assertSame(backglassComponent, service.getComponentFacade(ComponentType.b2sbackglass));
    assertSame(freezyComponent, service.getComponentFacade(ComponentType.freezy));
    assertSame(flexDMDComponent, service.getComponentFacade(ComponentType.flexdmd));
    assertSame(serumComponent, service.getComponentFacade(ComponentType.serum));
    assertSame(dofLinxComponent, service.getComponentFacade(ComponentType.doflinx));
    assertSame(dofComponent, service.getComponentFacade(ComponentType.dof));
  }

  // ---- helper ----

  private Component componentWithType(ComponentType type) {
    Component c = new Component();
    c.setType(type);
    c.setInstalledVersion("?");
    return c;
  }
}
