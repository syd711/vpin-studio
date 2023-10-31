package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.components.InstallLogRepresentation;
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

  ComponentServiceClient(VPinStudioClient client) {
    super(client);
  }

  public ComponentRepresentation getComponent(ComponentType type) {
    return getRestClient().get(API + "components/" + type, ComponentRepresentation.class);
  }

  public List<ComponentRepresentation> getComponents() {
    return Arrays.asList(getRestClient().get(API + "components", ComponentRepresentation[].class));
  }

  public boolean setVersion(ComponentType type, String version) throws Exception {
    return getRestClient().put(API + "components/setversion/" + type.name() + "/" + version, new HashMap<>(), Boolean.class);
  }

  public InstallLogRepresentation install(ComponentType type, String artifactName) throws Exception {
    try {
      return getRestClient().post(API + "components/install/" + type + "/" + artifactName, new HashMap<>(), InstallLogRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed execute update installation: " + e.getMessage(), e);
      throw new Exception("Installation failed");
    }
  }

  public InstallLogRepresentation simulate(ComponentType type, String artifactName) throws Exception {
    try {
      return getRestClient().post(API + "components/simulate/" + type + "/" + artifactName, new HashMap<>(), InstallLogRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed execute update installation: " + e.getMessage(), e);
      throw new Exception("Installation failed");
    }
  }

  public boolean clearCache() {
    return getRestClient().get(API + "components/clearcache", Boolean.class);
  }
}
