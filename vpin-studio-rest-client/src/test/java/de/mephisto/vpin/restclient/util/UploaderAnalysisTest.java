package de.mephisto.vpin.restclient.util;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UploaderAnalysisTest {

  private UploaderAnalysis newAnalysisForArchive(String archiveName, String... fileNames) {
    UploaderAnalysis analysis = new UploaderAnalysis(true, new File(archiveName));
    for (String fileName : fileNames) {
      analysis.analyze(fileName, false, 0);
    }
    return analysis;
  }

  private UploaderAnalysis newAnalysis(String... fileNames) {
    return newAnalysisForArchive("dummy.zip", fileNames);
  }

  // ---------------------------------------------------------------------
  // getRomFromPupPack() / getPUPPackFolder()
  // ---------------------------------------------------------------------

  @Test
  public void testRomFromPupPackViaScriptOnly() {
    UploaderAnalysis analysis = newAnalysis(
        "MyGame/scriptonly.txt",
        "MyGame/table.vpx");

    assertEquals("MyGame", analysis.getRomFromPupPack());
    assertEquals("MyGame", analysis.getPUPPackFolder());
  }

  @Test
  public void testRomFromPupPackPrefersShorterBatOverPupPath() {
    UploaderAnalysis analysis = newAnalysis(
        "MyGame/options.bat",
        "MyGame/sub/options/screen.pup");

    // fewer path segments wins
    assertEquals("MyGame", analysis.getRomFromPupPack());
  }

  @Test
  public void testRomFromPupPackReturnsNullWhenUnsupported() {
    UploaderAnalysis analysis = new UploaderAnalysis(false, new File("dummy.zip"));
    analysis.analyze("MyGame/scriptonly.txt", false, 0);

    assertNull(analysis.getRomFromPupPack());
  }

  @Test
  public void testRomFromPupPackReturnsNullWithoutRelevantFiles() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.vpx");
    assertNull(analysis.getRomFromPupPack());
  }

  // ---------------------------------------------------------------------
  // getPupPackRootDirectory()
  // ---------------------------------------------------------------------

  @Test
  public void testPupPackRootDirectoryResolved() {
    UploaderAnalysis analysis = newAnalysis(
        "MyPack/screens.pup",
        "MyPack/Options/options.bat");

    // shortest matching path wins, so the "Options" subfolder is excluded
    assertEquals("MyPack/", analysis.getPupPackRootDirectory());
  }

  @Test
  public void testPupPackRootDirectoryNullWhenParentFolderHasWhitespace() {
    UploaderAnalysis analysis = newAnalysis("My Pack/screens.pup");
    assertNull(analysis.getPupPackRootDirectory());
  }

  @Test
  public void testPupPackRootDirectoryNullWithoutMarkerFiles() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.vpx");
    assertNull(analysis.getPupPackRootDirectory());
  }

  // ---------------------------------------------------------------------
  // getAltSoundFolder()
  // ---------------------------------------------------------------------

  @Test
  public void testAltSoundFolderResolved() {
    UploaderAnalysis analysis = newAnalysis("MyGame/altsound/altsound.csv");
    assertEquals("MyGame/altsound", analysis.getAltSoundFolder());
  }

  @Test
  public void testAltSoundFolderNullWithoutAltSoundFiles() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.vpx");
    assertNull(analysis.getAltSoundFolder());
  }

  // ---------------------------------------------------------------------
  // getRomFromArchive()
  // ---------------------------------------------------------------------

  @Test
  public void testRomFromArchiveSelfExcludesNestedZipByDefault() {
    // analyze() auto-registers every ".zip" entry as an excluded file, so right
    // after analysis a nested zip is invisible to getRomFromArchive() ...
    UploaderAnalysis analysis = newAnalysis("roms/mygame.zip");
    assertNull(analysis.getRomFromArchive());
  }

  @Test
  public void testRomFromArchiveResolvesBaseNameOfZipOnceExclusionsAreReset() {
    // ... which is why callers (see PupPacksResource) reset/replace the exclusions
    // via setExclusions() before reading getRomFromArchive().
    UploaderAnalysis analysis = newAnalysis("roms/mygame.zip");
    analysis.resetExclusions();
    assertEquals("mygame", analysis.getRomFromArchive());
  }

  @Test
  public void testRomFromArchiveNullWithoutZip() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.vpx");
    assertNull(analysis.getRomFromArchive());
  }

  // ---------------------------------------------------------------------
  // getPatchFile()
  // ---------------------------------------------------------------------

  @Test
  public void testGetPatchFile() {
    UploaderAnalysis analysis = newAnalysis("patches/update.dif", "MyGame/table.vpx");
    assertEquals("patches/update.dif", analysis.getPatchFile());
  }

  @Test
  public void testGetPatchFileNull() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.vpx");
    assertNull(analysis.getPatchFile());
  }

  // ---------------------------------------------------------------------
  // getFileNameForAssetType() / getFileNamesForAssetType() / getFileNamesForExtension()
  // ---------------------------------------------------------------------

  @Test
  public void testGetFileNameForAssetType() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.vpx", "MyGame/table.cfg");
    assertEquals("table.vpx", analysis.getFileNameForAssetType(AssetType.VPX));
    assertEquals("table.cfg", analysis.getFileNameForAssetType(AssetType.CFG));
    assertNull(analysis.getFileNameForAssetType(AssetType.VPT));
  }

  @Test
  public void testGetFileNameForAssetTypeIniSkipsAltsoundIni() {
    UploaderAnalysis analysis = newAnalysis("MyGame/altsound.ini", "MyGame/table.ini");
    assertEquals("table.ini", analysis.getFileNameForAssetType(AssetType.INI));
  }

  @Test
  public void testGetFileNamesForAssetType() {
    UploaderAnalysis analysis = newAnalysis("MyGame/a.pal", "MyGame/b.pal", "MyGame/c.vni");
    List<String> pals = analysis.getFileNamesForAssetType(AssetType.PAL);
    assertEquals(2, pals.size());
    assertTrue(pals.contains("a.pal"));
    assertTrue(pals.contains("b.pal"));
  }

  @Test
  public void testGetFileNamesForExtension() {
    UploaderAnalysis analysis = newAnalysis("MyGame/a.mp3", "MyGame/b.mp3", "MyGame/c.wav");
    List<String> mp3s = analysis.getFileNamesForExtension("mp3");
    assertEquals(2, mp3s.size());
  }

  @Test
  public void testGetFileNameWithPathForExtension() {
    UploaderAnalysis analysis = newAnalysis("MyGame/sub/table.directb2s");
    assertEquals("MyGame/sub/table.directb2s", analysis.getFileNameWithPathForExtension("directb2s"));
  }

  // ---------------------------------------------------------------------
  // exclusions
  // ---------------------------------------------------------------------

  @Test
  public void testExcludedFilesAreFilteredOut() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.vpx", "MyGame/table.cfg");
    analysis.setExclusions(Arrays.asList("MyGame/table.cfg"), Arrays.asList());

    assertNull(analysis.getFileNameForAssetType(AssetType.CFG));
    assertNotNull(analysis.getFileNameForAssetType(AssetType.VPX));
    assertEquals(1, analysis.getExcludedFiles().size());
    assertEquals(1, analysis.getExclusions().size());
  }

  @Test
  public void testExcludedFoldersFilterNestedFiles() {
    UploaderAnalysis analysis = newAnalysis("MyGame/extra/notes.txt", "MyGame/table.vpx");
    analysis.setExclusions(Arrays.asList(), Arrays.asList("MyGame/extra"));

    List<String> vpx = analysis.getFileNamesForExtension("vpx");
    assertEquals(1, vpx.size());
    List<String> txt = analysis.getFileNamesForExtension("txt");
    assertTrue(txt.isEmpty());
  }

  @Test
  public void testResetExclusionsClearsFilters() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.cfg");
    analysis.setExclusions(new ArrayList<>(Arrays.asList("MyGame/table.cfg")), new ArrayList<>());
    assertEquals(1, analysis.getExcludedFiles().size());

    analysis.resetExclusions();
    assertTrue(analysis.getExcludedFiles().isEmpty());
    assertNotNull(analysis.getFileNameForAssetType(AssetType.CFG));
  }

  // ---------------------------------------------------------------------
  // reset() / getFileNamesWithPath() / getFoldersWithPath()
  // ---------------------------------------------------------------------

  @Test
  public void testResetClearsAnalyzedEntries() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.vpx");
    assertFalse(analysis.getFileNamesWithPath().isEmpty());
    assertFalse(analysis.getFoldersWithPath().isEmpty());

    analysis.reset();
    assertTrue(analysis.getFileNamesWithPath().isEmpty());
    assertTrue(analysis.getFoldersWithPath().isEmpty());
  }

  // ---------------------------------------------------------------------
  // isArchive()
  // ---------------------------------------------------------------------

  @Test
  public void testIsArchiveTrueForZip() {
    UploaderAnalysis analysis = newAnalysisForArchive("MyGame.zip");
    assertTrue(analysis.isArchive());
  }

  @Test
  public void testIsArchiveFalseForVpx() {
    UploaderAnalysis analysis = newAnalysisForArchive("MyGame.vpx");
    assertFalse(analysis.isArchive());
  }

  // ---------------------------------------------------------------------
  // isVpxOrFpTable() / isVpxTable() / isFpTable() / isPatch() / getEmulatorType()
  // ---------------------------------------------------------------------

  @Test
  public void testIsVpxTableByDirectExtension() {
    UploaderAnalysis analysis = newAnalysisForArchive("MyGame.vpx");
    assertTrue(analysis.isVpxTable());
    assertTrue(analysis.isVpxOrFpTable());
    assertFalse(analysis.isFpTable());
    assertEquals(EmulatorType.VisualPinball, analysis.getEmulatorType());
  }

  @Test
  public void testIsFpTableByArchiveContent() {
    UploaderAnalysis analysis = newAnalysisForArchive("MyGame.zip", "MyGame/table.fpt");
    assertTrue(analysis.isFpTable());
    assertTrue(analysis.isVpxOrFpTable());
    assertEquals(EmulatorType.FuturePinball, analysis.getEmulatorType());
  }

  @Test
  public void testIsPatchByExtension() {
    UploaderAnalysis analysis = newAnalysisForArchive("update.dif");
    assertTrue(analysis.isPatch());
  }

  @Test
  public void testGetEmulatorTypeNullWhenUnresolvable() {
    UploaderAnalysis analysis = newAnalysisForArchive("readme.txt");
    assertNull(analysis.getEmulatorType());
  }

  // ---------------------------------------------------------------------
  // validateAssetTypeInArchive()
  // ---------------------------------------------------------------------

  @Test
  public void testValidateAssetTypeInArchiveVpxPresent() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.vpx");
    assertNull(analysis.validateAssetTypeInArchive(AssetType.VPX));
  }

  @Test
  public void testValidateAssetTypeInArchiveVpxMissing() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.vpt");
    assertNotNull(analysis.validateAssetTypeInArchive(AssetType.VPX));
  }

  @Test
  public void testValidateAssetTypeInArchiveDirectB2s() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.directb2s");
    assertNull(analysis.validateAssetTypeInArchive(AssetType.DIRECTB2S));
  }

  @Test
  public void testValidateAssetTypeInArchiveDif() {
    UploaderAnalysis analysis = newAnalysis("patch.dif");
    assertNull(analysis.validateAssetTypeInArchive(AssetType.DIF));
  }

  @Test
  public void testValidateAssetTypeInArchivePupPack() {
    UploaderAnalysis analysis = newAnalysis("MyPack/screens.pup");
    assertNull(analysis.validateAssetTypeInArchive(AssetType.PUP_PACK));
  }

  @Test
  public void testValidateAssetTypeInArchivePupPackMissing() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.vpx");
    assertNotNull(analysis.validateAssetTypeInArchive(AssetType.PUP_PACK));
  }

  // ---------------------------------------------------------------------
  // isBackglass() / isRes()
  // ---------------------------------------------------------------------

  @Test
  public void testIsBackglassAndIsRes() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.directb2s", "MyGame/table.res");
    assertTrue(analysis.isBackglass());
    assertTrue(analysis.isRes());
  }

  // ---------------------------------------------------------------------
  // isMusic() / isDMD()
  // ---------------------------------------------------------------------

  @Test
  public void testIsMusicTrueWithMusicFiles() {
    UploaderAnalysis analysis = newAnalysis("MyGame/music/theme.mp3");
    assertTrue(analysis.isMusic());
  }

  @Test
  public void testIsMusicFalseWhenAltSound() {
    // altsound.csv files make isAltSound() true, which suppresses isMusic()
    UploaderAnalysis analysis = newAnalysis("MyGame/altsound.csv", "MyGame/theme.mp3");
    assertFalse(analysis.isMusic());
  }

  @Test
  public void testIsDMDTrueForUltraDMDFolder() {
    UploaderAnalysis analysis = newAnalysis("MyGame/UltraDMD/video.mp4");
    assertTrue(analysis.isDMD());
    assertEquals("MyGame/UltraDMD", analysis.getDMDPath());
  }

  @Test
  public void testIsDMDFalseForPlainDMDFolder() {
    // segment literally named "DMD" is explicitly excluded
    UploaderAnalysis analysis = newAnalysis("MyGame/DMD/image.png");
    assertFalse(analysis.isDMD());
  }

  // ---------------------------------------------------------------------
  // getRelativeMusicPath() / getRelativeMusicPathWithoutMusicFolder()
  // ---------------------------------------------------------------------

  @Test
  public void testGetRelativeMusicPath() {
    UploaderAnalysis analysis = newAnalysis("MyGame/music/theme.mp3");
    assertEquals("MyGame/music/", analysis.getRelativeMusicPath());
    assertEquals("music/", analysis.getRelativeMusicPathWithoutMusicFolder());
  }

  @Test
  public void testGetRelativeMusicPathIgnoresPupPackAudio() {
    UploaderAnalysis analysis = newAnalysis(
        "MyPack/screens.pup",
        "MyPack/sound/theme.mp3");
    assertNull(analysis.getRelativeMusicPath());
  }

  // ---------------------------------------------------------------------
  // getPopperMediaFiles()
  // ---------------------------------------------------------------------

  @Test
  public void testGetPopperMediaFilesForMenuScreen() {
    // files belonging to a resolved pup pack are excluded from the generic media
    // pack lookup, so keep these outside of any pup pack root directory
    UploaderAnalysis analysis = newAnalysis(
        "Menu/fulldmd.png",
        "Menu/apron.png");

    List<String> menuFiles = analysis.getPopperMediaFiles(VPinScreen.Menu);
    assertEquals(2, menuFiles.size());
  }

  @Test
  public void testGetPopperMediaFilesEmptyWhenPupPacksUnsupported() {
    UploaderAnalysis analysis = new UploaderAnalysis(false, new File("dummy.zip"));
    analysis.analyze("MyPack/Menu/apron.png", false, 0);

    assertTrue(analysis.getPopperMediaFiles(VPinScreen.Menu).isEmpty());
  }

  // ---------------------------------------------------------------------
  // getReadMeText()
  // ---------------------------------------------------------------------

  @Test
  public void testGetReadMeTextNullByDefault() {
    UploaderAnalysis analysis = newAnalysis("MyGame/table.vpx");
    assertNull(analysis.getReadMeText());
  }
}
