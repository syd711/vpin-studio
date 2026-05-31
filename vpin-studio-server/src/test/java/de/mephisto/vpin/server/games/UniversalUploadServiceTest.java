package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.backups.adapters.vpa.VpaService;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.doflinx.DOFLinxService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.fp.FuturePinballService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.highscores.HighscoreBackupService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.music.MusicService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.vps.VpsService;
import de.mephisto.vpin.server.vpinmame.VPinMameRomAliasService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UniversalUploadServiceTest {

  @Mock private GameService gameService;
  @Mock private DMDService dmdService;
  @Mock private VPinMameService vPinMameService;
  @Mock private MameService mameService;
  @Mock private FuturePinballService futurePinballService;
  @Mock private AltColorService altColorService;
  @Mock private AltSoundService altSoundService;
  @Mock private MusicService musicService;
  @Mock private GameMediaService gameMediaService;
  @Mock private PupPacksService pupPacksService;
  @Mock private VpsService vpsService;
  @Mock private DiscordService discordService;
  @Mock private VPinMameRomAliasService VPinMameRomAliasService;
  @Mock private PreferencesService preferencesService;
  @Mock private EmulatorService emulatorService;
  @Mock private GameLifecycleService gameLifecycleService;
  @Mock private HighscoreBackupService highscoreBackupService;
  @Mock private DOFLinxService dofLinxService;
  @Mock private VpaService vpaService;
  @Mock private FrontendService frontendService;

  @InjectMocks
  private UniversalUploadService universalUploadService;

  @Test
  void create_returnsEmptyDescriptor() {
    UploadDescriptor descriptor = universalUploadService.create();

    assertThat(descriptor).isNotNull();
    assertThat(descriptor.getGameId()).isEqualTo(0);
  }

  @Test
  void error_setsErrorMessage() {
    UploadDescriptor descriptor = universalUploadService.error("something went wrong");

    assertThat(descriptor).isNotNull();
    assertThat(descriptor.getError()).isEqualTo("something went wrong");
  }

  @Test
  void create_withMultipartFile_setsGameIdToZero() {
    var multipart = mock(org.springframework.web.multipart.MultipartFile.class);
    when(multipart.getOriginalFilename()).thenReturn("table.vpx");

    UploadDescriptor descriptor = universalUploadService.create(multipart);

    assertThat(descriptor.getGameId()).isEqualTo(0);
    assertThat(descriptor.getOriginalUploadFileName()).isEqualTo("table.vpx");
  }

  @Test
  void create_withMultipartFileAndGameId_setsGameId() {
    var multipart = mock(org.springframework.web.multipart.MultipartFile.class);
    when(multipart.getOriginalFilename()).thenReturn("table.vpx");

    UploadDescriptor descriptor = universalUploadService.create(multipart, 42);

    assertThat(descriptor.getGameId()).isEqualTo(42);
  }

  @Test
  void resolveLinks_nonLinkFilename_doesNothing() throws Exception {
    UploadDescriptor descriptor = new UploadDescriptor();
    descriptor.setOriginalUploadFileName("regular_file.zip");
    descriptor.setTempFilename("/tmp/regular_file.zip");

    // No exception and vpsService not called
    universalUploadService.resolveLinks(descriptor);

    verifyNoInteractions(vpsService);
    assertThat(descriptor.getOriginalUploadFileName()).isEqualTo("regular_file.zip");
  }
}
