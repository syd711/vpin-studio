package de.mephisto.vpin.server.system;


import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.system.FolderRepresentation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "folder")
public class FolderChooserResource {
  private final static Logger LOG = LoggerFactory.getLogger(FolderChooserResource.class);


  @GetMapping("roots")
  public List<FolderRepresentation> getRoots() {
    File[] roots = File.listRoots();
    List<FolderRepresentation> result = new ArrayList<>();
    for (File root : roots) {
      result.add(listSubfolders(root.getAbsolutePath()));
    }
    return result;
  }

  /**
   * Lists subfolders inside the given path.
   * <p>
   * Example:
   * GET /api/folders?path=/Users/myuser
   */
  @GetMapping
  public FolderRepresentation listSubfolders(@RequestParam(value = "path", required = false) String path) {
    path = URLDecoder.decode(path, StandardCharsets.UTF_8);
    try {
      File baseDir = (path == null || path.isBlank())
          ? new File(System.getProperty("user.home"))
          : new File(path);

      FolderRepresentation folder = buildFolder(baseDir);
      List<FolderRepresentation> children = folder.getChildren();
      for (FolderRepresentation child : children) {
        FolderRepresentation loadedChild = buildFolder(new File(child.getPath()));
        child.setChildren(loadedChild.getChildren());
      }
      return folder;
    }
    catch (Exception e) {
      LOG.error("Failed to read folder {}: {}", path, e.getMessage(), e);
    }
    return null;
  }

  @NotNull
  private static FolderRepresentation buildFolder(File baseDir) {
    FolderRepresentation folder = new FolderRepresentation(baseDir);
    if (baseDir.isHidden() && baseDir.getParentFile() != null) {
      return folder;
    }

    File[] files = baseDir.listFiles(File::isDirectory);
    if (files == null) {
      return folder;
    }
    folder.setChildren(Arrays.stream(files)
        .filter(f -> !f.isHidden())
        .filter(f -> !f.getName().startsWith("."))
        .map(file -> new FolderRepresentation(file))
        .collect(Collectors.toList()));

    return folder;
  }
}
