package de.mephisto.vpin.restclient.components;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*********************************************************************************************************************
 * Components
 ********************************************************************************************************************/
public class ComponentServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentServiceClient.class);

  public ComponentServiceClient(VPinStudioClient client) {
    super(client);
  }

  public ComponentRepresentation getComponent(ComponentType type) {
    return getRestClient().get(API + "components/" + type.name(), ComponentRepresentation.class);
  }

  public List<ComponentRepresentation> getComponents() {
    return Arrays.asList(getRestClient().get(API + "components", ComponentRepresentation[].class));
  }

  public boolean setVersion(ComponentType type, String version) throws Exception {
    return getRestClient().put(API + "components/setversion/" + type.name() + "/" + version, new HashMap<>(), Boolean.class);
  }

  public boolean ignoreVersion(ComponentType type, String version) throws Exception {
    return getRestClient().put(API + "components/ignoreversion/" + type.name() + "/" + version, new HashMap<>(), Boolean.class);
  }

  public ComponentActionLogRepresentation install(ComponentType type, String tag, String artifactName) throws Exception {
    try {
      return getRestClient().post(API + "components/install/" + type.name() + "/" + tag + "/" + artifactName, new HashMap<>(), ComponentActionLogRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed execute update installation: " + e.getMessage(), e);
      throw new Exception("Installation failed");
    }
  }

  public ComponentActionLogRepresentation check(ComponentType type, String tag, String artifactName, boolean forceDownload) throws Exception {
    try {
      return getRestClient().post(API + "components/check/" + type.name() + "/" + tag + "/" + artifactName + "/" + forceDownload, new HashMap<>(), ComponentActionLogRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed execute update diff: " + e.getMessage(), e);
      throw new Exception("Diff failed");
    }
  }

  public ComponentActionLogRepresentation simulate(ComponentType type, String tag, String artifactName) throws Exception {
    try {
      return getRestClient().post(API + "components/simulate/" + type.name() + "/" + tag + "/" + artifactName, new HashMap<>(), ComponentActionLogRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed execute update installation: " + e.getMessage(), e);
      throw new Exception("Installation failed");
    }
  }

  public boolean clearCache() {
    return getRestClient().get(API + "components/clearcache", Boolean.class);
  }
}