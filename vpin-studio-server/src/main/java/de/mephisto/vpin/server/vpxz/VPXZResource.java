package de.mephisto.vpin.server.vpxz;

import de.mephisto.vpin.restclient.vpxz.VPXZDescriptorRepresentation;
import de.mephisto.vpin.restclient.vpxz.VPXZPackageInfo;
import de.mephisto.vpin.restclient.vpxz.VPXZSourceRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobDescriptorFactory;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "vpxz")
public class VPXZResource {
  private final static Logger LOG = LoggerFactory.getLogger(VPXZResource.class);

  @Autowired
  private VPXZService vpxzService;

  @PostMapping("/create")
  public Boolean createVPXZ(@RequestBody VPXZDescriptor descriptor) {
    //this triggers the jobs creation and can include hundrets of new jobs, so run async
    new Thread(() -> {
      Thread.currentThread().setName("VPXZ Runner for source " + descriptor.getFilename());
      vpxzService.createVpxz(descriptor);
    }).start();
    return true;
  }

  @GetMapping()
  public List<VPXZDescriptorRepresentation> getVPXZFiles() {
    List<VPXZDescriptor> descriptors = vpxzService.getVPXZDescriptors();
    return toRepresentation(descriptors);
  }

  @GetMapping("/{sourceId}")
  public List<VPXZDescriptorRepresentation> getVPXZFiles(@PathVariable("sourceId") long sourceId) {
    List<VPXZDescriptor> descriptors = vpxzService.getVPXZDescriptors(sourceId);
    return toRepresentation(descriptors);
  }

  @NotNull
  private List<VPXZDescriptorRepresentation> toRepresentation(List<VPXZDescriptor> descriptors) {
    List<VPXZDescriptorRepresentation> result = new ArrayList<>();
    for (VPXZDescriptor VPXZDescriptor : descriptors) {
      VPXZDescriptorRepresentation descriptorRepresentation = toRepresentation(VPXZDescriptor);
      result.add(descriptorRepresentation);
    }
    return result;
  }

  @GetMapping("/sources")
  public List<VPXZSourceRepresentation> getSources() {
    return vpxzService.getVPXMobileSources().stream().map(source -> toRepresentation(source)).collect(Collectors.toList());
  }

  @DeleteMapping("/{sourceId}/{filename}")
  public boolean deleteVPXMobile(@PathVariable("sourceId") long sourceId,
                              @PathVariable("filename") String filename) {
    return vpxzService.deleteVPXZ(sourceId, filename);
  }

  @DeleteMapping("/source/{id}")
  public boolean deleteArchiveSource(@PathVariable("id") long id) {
    return vpxzService.deleteVPXMobileSource(id);
  }

  @GetMapping("/game/{id}")
  public List<VPXZDescriptorRepresentation> getVPXFilesForGame(@PathVariable("id") int gameId) {
    List<VPXZDescriptor> VPXZDescriptors = vpxzService.getVPXZDescriptorForGame(gameId);
    return toRepresentation(VPXZDescriptors);
  }

  @GetMapping("/invalidate")
  public boolean invalidateCache() {
    vpxzService.invalidateCache();
    getVPXZFiles();
    return true;
  }

  @GetMapping("/download/file/{sourceId}/{filename}")
  public void downloadVPXZFile(@PathVariable("sourceId") long sourceId,
                               @PathVariable("filename") String fn,
                               HttpServletResponse response) {
    InputStream in = null;
    OutputStream out = null;
    String filename = URLDecoder.decode(fn, StandardCharsets.UTF_8);

    try {
      VPXZDescriptor VPXZDescriptor = vpxzService.getVPXZDescriptors(sourceId, filename);
      VPXZSourceAdapter sourceAdapter = vpxzService.getVPXMobileSourceAdapter(sourceId);

      in = sourceAdapter.getVPXMobileInputStream(VPXZDescriptor);
      out = response.getOutputStream();
      IOUtils.copy(in, out);
      response.flushBuffer();
      in.close();
      out.close();

      LOG.info("Finished download of \"" + VPXZDescriptor.getTableDetails().getGameDisplayName() + "\"");
      invalidateCache();
    }
    catch (IOException ex) {
      LOG.info("Error writing archive to output stream. Filename was '{}'", filename, ex);
      throw new RuntimeException("IOError writing file to output stream");
    }
  }

  @PostMapping("/upload")
  public JobDescriptor uploadArchive(@RequestParam(value = "file", required = false) MultipartFile file,
                                     @RequestParam("objectId") Integer repositoryId) {
    try {
      if (file == null) {
        LOG.error("VPXZ upload request did not contain a file object.");
        return null;
      }
      VPXZSourceAdapter sourceAdapter = vpxzService.getVPXMobileSourceAdapter(repositoryId);
      File out = new File(sourceAdapter.getVPXZSource().getLocation(), file.getOriginalFilename());
      if (UploadUtil.upload(file, out)) {
        vpxzService.invalidateCache();
      }
    }
    catch (Exception e) {
      LOG.error("VPXZ upload failed: " + e.getMessage(), e);
      return JobDescriptorFactory.error("VPXZ upload failed: " + e.getMessage());
    }
    return JobDescriptorFactory.error(null);
  }

  @PostMapping("/save")
  public VPXZSourceRepresentation save(@RequestBody VPXZSourceRepresentation VPXZSourceRepresentation) {
    VPXZSource update = vpxzService.save(VPXZSourceRepresentation);
    return toRepresentation(update);
  }


  private VPXZSourceRepresentation toRepresentation(VPXZSource source) {
    VPXZSourceRepresentation representation = new VPXZSourceRepresentation();
    representation.setType(source.getType());
    representation.setLocation(source.getLocation());
    representation.setName(source.getName());
    representation.setId(source.getId());
    representation.setLogin(source.getLogin());
    representation.setPassword(source.getPassword());
    representation.setEnabled(source.isEnabled());
    representation.setSettings(source.getSettings());
    representation.setAuthenticationType(source.getAuthenticationType());
    return representation;
  }

  private VPXZDescriptorRepresentation toRepresentation(VPXZDescriptor descriptor) {
    VPXZSourceRepresentation source = toRepresentation(descriptor.getSource());
    VPXZDescriptorRepresentation representation = new VPXZDescriptorRepresentation();
    representation.setFilename(descriptor.getFilename());
    representation.setTableDetails(descriptor.getTableDetails());
    representation.setCreatedAt(descriptor.getCreatedAt());
    representation.setSize(descriptor.getSize());
    representation.setArchiveType(FilenameUtils.getExtension(descriptor.getFilename()));

    VPXZPackageInfo packageInfo = descriptor.getPackageInfo();
    representation.setPackageInfo(descriptor.getPackageInfo());
    representation.setSource(source);
    return representation;
  }
}
