package de.mephisto.vpin.server.vpauthenticators;

import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "vpauthentication")
public class VPAuthenticationResource {

  @Autowired
  private VPAuthenticationService vpAuthenticationService;

  @PostMapping("/{authenticationProvider}/login")
  public String login(@PathVariable("authenticationProvider") AuthenticationProvider authenticationProvider, @RequestBody Map<String, String> data) throws Exception {
    String login = data.get("login");
    String password = data.get("password");
    return vpAuthenticationService.login(authenticationProvider, login, password);
  }

}
