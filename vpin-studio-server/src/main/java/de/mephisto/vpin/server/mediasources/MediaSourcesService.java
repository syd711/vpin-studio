package de.mephisto.vpin.server.mediasources;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.mediasources.MediaSource;
import de.mephisto.vpin.restclient.mediasources.MediaSourcesSettings;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.MimeTypeUtil;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class MediaSourcesService implements PreferenceChangedListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(MediaSourcesService.class);

  @Autowired
  private PreferencesService preferencesService;

  private MediaSourcesSettings mediaSourcesSettings;

  @Nullable
  public MediaSource getMediaSource(String sourceId) {
    Optional<MediaSource> first = mediaSourcesSettings.getMediaSources().stream().filter(m -> m.getId().equalsIgnoreCase(sourceId)).findFirst();
    return first.orElse(null);
  }

  @NonNull
  public List<MediaSource> getMediaSources() {
    return mediaSourcesSettings.getMediaSources();
  }

  public boolean deleteMediaSource(String id) throws Exception {
    List<MediaSource> filtered = mediaSourcesSettings.getMediaSources().stream().filter(m -> !m.getId().equalsIgnoreCase(id)).collect(Collectors.toList());
    mediaSourcesSettings.setMediaSources(filtered);
    preferencesService.savePreference(mediaSourcesSettings);
    return true;
  }

  public MediaSource save(MediaSource mediaSource) throws Exception {
    List<MediaSource> filtered = new ArrayList<>(mediaSourcesSettings.getMediaSources().stream().filter(m -> !m.getId().equalsIgnoreCase(mediaSource.getId())).collect(Collectors.toList()));
    filtered.add(mediaSource);
    mediaSourcesSettings.setMediaSources(filtered);
    preferencesService.savePreference(mediaSourcesSettings);
    return mediaSource;
  }

  @NonNull
  public List<TableAsset> search(@NonNull MediaSource mediaSource, @NonNull EmulatorType emulator, @NonNull VPinScreen screen, int gameId, @NonNull String term) {
    if (mediaSource.isEnabled()) {
      File folder = new File(mediaSource.getLocation());
      if (folder.exists() && folder.isDirectory()) {
        List<File> result = new ArrayList<>();
        FileUtils.findFileRecursive(folder, Arrays.asList("png", "apng", "mov", "mp4", "mp3", "ogg", "mkv"), term, result);
        return result.stream().map(f -> {
          return toTableAsset(mediaSource, emulator, screen, f);
        }).collect(Collectors.toList());
      }
    }
    return Collections.emptyList();
  }

  public Optional<TableAsset> get(@NonNull MediaSource mediaSource, @NonNull EmulatorType emulatorType, @NonNull VPinScreen screen, int gameId, @NonNull String folder, @NonNull String name) {
    String folderName = folder.substring("file:///".length());
    File f = new File(folderName, name);
    return Optional.of(toTableAsset(mediaSource, emulatorType, screen, f));
  }

  @NotNull
  private static TableAsset toTableAsset(@NotNull MediaSource mediaSource, @NotNull EmulatorType emulator, @NotNull VPinScreen screen, @NonNull File f) {
    String filename = f.getName();

    TableAsset asset = new TableAsset();
    asset.setEmulator(null);
    asset.setScreen(screen.getSegment());

    asset.setMimeType(MimeTypeUtil.determineMimeType(FilenameUtils.getExtension(filename).toLowerCase()));

    String url = URLEncoder.encode("file:///" + f.getAbsolutePath().replaceAll("\\\\", "/"), StandardCharsets.UTF_8);
    asset.setUrl(url);
    asset.setSourceId(mediaSource.getId());
    asset.setName(filename);
    asset.setAuthor(mediaSource.getName());
    asset.setLength(filename.length());

    return asset;
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.MEDIA_SOURCES_SETTINGS.equalsIgnoreCase(propertyName)) {
      this.mediaSourcesSettings = preferencesService.getJsonPreference(PreferenceNames.MEDIA_SOURCES_SETTINGS, MediaSourcesSettings.class);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferenceChanged(PreferenceNames.MEDIA_SOURCES_SETTINGS, null, null);
    preferencesService.addChangeListener(this);
  }
}
