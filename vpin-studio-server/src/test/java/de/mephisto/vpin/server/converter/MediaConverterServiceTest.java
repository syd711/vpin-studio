package de.mephisto.vpin.server.converter;

import de.mephisto.vpin.restclient.converter.MediaConversionCommand;
import de.mephisto.vpin.restclient.converter.MediaOperationResult;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.GameMediaService;
import de.mephisto.vpin.server.playlists.PlaylistMediaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MediaConverterServiceTest {

  @Mock
  private FrontendService frontendService;
  @Mock
  private GameMediaService gameMediaService;
  @Mock
  private PlaylistMediaService playlistMediaService;

  @InjectMocks
  private MediaConverterService mediaConverterService;

  // --- afterPropertiesSet ---

  @Test
  void afterPropertiesSet_loadsImageAndFfmpegCommands_whenNotPopper() throws Exception {
    Frontend frontend = mock(Frontend.class);
    when(frontendService.getFrontend()).thenReturn(frontend);
    when(frontend.getFrontendType()).thenReturn(FrontendType.Standalone);

    mediaConverterService.afterPropertiesSet();

    List<MediaConversionCommand> commands = mediaConverterService.getCommandList();
    assertThat(commands).isNotEmpty();
    assertThat(commands.stream().anyMatch(c -> c.getName().contains("Rotate"))).isTrue();
  }

  @Test
  void afterPropertiesSet_includesImageCommands_whenNotPopper() throws Exception {
    Frontend frontend = mock(Frontend.class);
    when(frontendService.getFrontend()).thenReturn(frontend);
    when(frontend.getFrontendType()).thenReturn(FrontendType.Standalone);

    mediaConverterService.afterPropertiesSet();

    List<MediaConversionCommand> commands = mediaConverterService.getCommandList();
    long imageCommandCount = commands.stream()
        .filter(c -> c.getType() == MediaConversionCommand.TYPE_IMAGE)
        .count();
    assertThat(imageCommandCount).isEqualTo(3);
  }

  @Test
  void afterPropertiesSet_clearsCommandsOnReload() throws Exception {
    Frontend frontend = mock(Frontend.class);
    when(frontendService.getFrontend()).thenReturn(frontend);
    when(frontend.getFrontendType()).thenReturn(FrontendType.Standalone);

    mediaConverterService.afterPropertiesSet();
    int firstCount = mediaConverterService.getCommandList().size();
    mediaConverterService.afterPropertiesSet();
    int secondCount = mediaConverterService.getCommandList().size();

    assertThat(secondCount).isEqualTo(firstCount);
  }

  // --- getCommandList ---

  @Test
  void getCommandList_returnsEmptyList_beforeInitialization() {
    assertThat(mediaConverterService.getCommandList()).isEmpty();
  }

  // --- convert with TYPE_IMAGE ---

  @Test
  void convert_setsErrorResult_whenImageFileDoesNotExist(@TempDir Path tempDir) throws Exception {
    MediaConversionCommand command = new MediaConversionCommand("Rotate 90°")
        .setImageArgs(MediaConversionCommand.ImageOp.ROTATE_90);
    MediaOperationResult result = new MediaOperationResult();
    File nonExistentFile = tempDir.resolve("nonexistent.png").toFile();

    mediaConverterService.convert(command, result, nonExistentFile);

    assertThat(result.getResult()).contains("Image conversion failed");
  }

  // --- convert with unknown type ---

  @Test
  void convert_setsUnsupportedMessage_forUnknownCommandType() throws Exception {
    MediaConversionCommand command = new MediaConversionCommand("Unknown");
    MediaOperationResult result = new MediaOperationResult();
    File file = new File("test.mp4");

    mediaConverterService.convert(command, result, file);

    assertThat(result.getResult()).contains("Not supported");
  }
}
