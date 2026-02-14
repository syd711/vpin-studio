package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.TableAssetSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "assetsources")
public class TableAssetSourcesResource {

  @Autowired
  private TableAssetSourcesService tableAssetSourcesService;

  @GetMapping
  public List<TableAssetSource> getSources() {
    return tableAssetSourcesService.getAssetSources();
  }

  @GetMapping("/default")
  public TableAssetSource getDefaultSource() {
    return tableAssetSourcesService.getDefaultAssetSource();
  }

  @GetMapping("/{sourceId}")
  public TableAssetSource getSource(@PathVariable("sourceId") String sourceId) {
    return tableAssetSourcesService.getAssetSource(sourceId);
  }

  @DeleteMapping("/{sourceId}")
  public boolean deleteMediaSource(@PathVariable("sourceId") String sourceId) throws Exception {
    return tableAssetSourcesService.deleteAssetSource(sourceId);
  }

  @PostMapping("/save")
  public TableAssetSource save(@RequestBody TableAssetSource tableAssetSource) throws Exception {
    return tableAssetSourcesService.save(tableAssetSource);
  }
}
