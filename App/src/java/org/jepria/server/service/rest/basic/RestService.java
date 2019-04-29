package org.jepria.server.service.rest.basic;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jepria.CastMap;
import org.jepria.CastMap.CastOnGetException;
import org.jepria.TypedValueParser;
import org.jepria.TypedValueParser.TypedValueParseException;
import org.jepria.TypedValueParserImpl;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

// регистрационный подход: в прикладной реализации метода регистрируются все возможные entity.
// в отличие от лобового подхода, все запросы незарегистрированных энтитей будут выдавать 404
// Преимущество: каждой энтити при регистрации можно указать не только дата экстрактор, но и некую метаинфу: для сваггера, для джсшемы и т.д.
public class RestService extends HttpServlet {

  private static final long serialVersionUID = 1L;

  public RestService() {}

  private final Map<String, EndpointMethod> optionExtractors = new HashMap<>();

  // по сути, Supplier, который позволяет выбрасывать exception
  // = по сути, Callable, который не связан с concurrent
  public static abstract class EndpointMethod {
    public abstract Object getData(HttpServletRequest request, CastMap<String, ?> params) throws Exception;
    public EndpointMethod() {}
  }

  protected void registerOptions(String entity, EndpointMethod endpointMethod) {
    if (entity == null) {
      throw new NullPointerException("Cannot register null entity");
    }

    if (endpointMethod == null) {
      throw new NullPointerException("Cannot register null data extractor");
    }

    if (optionExtractors.containsKey(entity)) {
      throw new IllegalArgumentException("The entity has already been registered, cannot register twice");
    }

    optionExtractors.put(entity, endpointMethod);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {

    final String path = req.getPathInfo();

    if (path != null) {
      if (path.startsWith("/option/")) {

        final String entity = path.substring("/option/".length());

        EndpointMethod endpointMethod = optionExtractors.get(entity);

        if (endpointMethod != null) {

          // inject the extractor fields

          final Object result;

          try {
            // TODO better to pass the request here or put it into ThreadLocal like GWT does?
            result = endpointMethod.getData(req, getParameterMap(req));
            
          } catch (CastOnGetException e) {
            
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("The parameter " + e.getKey() + " expected to be of type " + e.getCastTo().getCanonicalName());
            response.flushBuffer();
            
            return;
            
          } catch (Exception e) {
            // TODO handle properly
            throw new RuntimeException(e);
          }

          if (result != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().println(new Gson().toJson(result));
          } else {
            // empty response, write nothing, do not set content type as far as there is no content (however not a NO_CONTENT status)
            response.setStatus(HttpServletResponse.SC_OK);
          }

        } else {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        response.flushBuffer();
      }
    }

  }

  /**
   * @param req
   * @return or else empty map, non null
   */
  // return namely Map<String, String>, not Map<String, Object>
  protected Map<String, String> getRequestParameterMap(HttpServletRequest req) {
    final Map<String, String> map = new HashMap<>();

    if (req != null) {
      Map<String, String[]> original = req.getParameterMap();
      if (original != null) {
        for (String key: original.keySet()) {
          String[] values = original.get(key);
          if (values != null) {
            map.put(key, values[values.length - 1]);
          }
        }
      }
    }

    return map;
  }

  /**
   * @param req
   * @return or else empty map, non null
   */
  protected Map<String, Object> getBodyParameterMap(HttpServletRequest req) {
    final Map<String, Object> map = new HashMap<>();

    if (req != null) {
      try {
        Map<String, Object> gsonMap = new Gson().fromJson(new InputStreamReader(req.getInputStream()), new TypeToken<Map<String, Object>>() { }.getType());
        if (gsonMap != null) {
          map.putAll(gsonMap);
        }
      } catch (JsonIOException | JsonSyntaxException | IOException e) {
        // TODO Auto-generated catch block
        throw new RuntimeException(e);
      }
    }

    return map;
  }
  
  protected CastMap<String, ?> getParameterMap(HttpServletRequest req) {
    final ParamCastMap map = new ParamCastMap();
    map.putAll(getRequestParameterMap(req));
    map.putAll(getBodyParameterMap(req));
    return map;
  }
  
  protected TypedValueParser getTypedValueParser() {
    return new TypedValueParserImpl();
  }
  
  // local class
  protected class ParamCastMap extends HashMap<String, Object> implements CastMap<String, Object> {

    private static final long serialVersionUID = -4621908477271689859L;
    
    @Override
    public Integer getInteger(Object key) {
      try {
        return getTypedValueParser().parse(get(key), Integer.class);
      } catch (TypedValueParseException e) {
        throw new CastOnGetException(key, Integer.class);
      }
    }

    @Override
    public String getString(Object key) {
      try {
        return getTypedValueParser().parse(get(key), String.class);
      } catch (TypedValueParseException e) {
        throw new CastOnGetException(key, String.class);
      }
    }

    @Override
    public BigDecimal getBigDecimal(Object key) {
      try {
        return getTypedValueParser().parse(get(key), BigDecimal.class);
      } catch (TypedValueParseException e) {
        throw new CastOnGetException(key, BigDecimal.class);
      }
    }
  }
}


