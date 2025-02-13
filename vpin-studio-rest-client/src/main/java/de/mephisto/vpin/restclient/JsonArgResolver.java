package de.mephisto.vpin.restclient;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonArgResolver implements HandlerMethodArgumentResolver{

  private static final String JSONBODYATTRIBUTE = "JSON_REQUEST_BODY";

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(JsonArg.class);
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
    String name = parameter.getParameterName();
    Map<String, Object> values = getRequestBody(webRequest);
    return values.get(name);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getRequestBody(NativeWebRequest webRequest){
    HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
    HashMap<String, Object> jsonBody = (HashMap<String, Object>) servletRequest.getAttribute(JSONBODYATTRIBUTE);
    if (jsonBody==null){
      try {
          String body = IOUtils.toString(servletRequest.getInputStream(), Charset.forName("UTF-8"));
          jsonBody = new ObjectMapper().readValue(body, HashMap.class);
          servletRequest.setAttribute(JSONBODYATTRIBUTE, jsonBody);
        } catch (IOException e) {
        throw new RuntimeException("JsonArg, cannot parse body into Map");
      }
    }
    return jsonBody;
  }

}