package com.technology.jep.jepria.server.security.tomcat;

import static com.technology.jep.jepria.shared.util.JepRiaUtil.isEmpty;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.technology.jep.jepria.server.util.JepServerUtil;

/**
 * GET-параметры запроса:<br>
 * <br>
 * username<br>
 * password<br>
 * url - либо полный URL в виде <code>http://host[:port]/resource?query</code>, либо только часть относительно сервера <code>/resource?query</code><br>  
 */
public class AutoLogonServlet extends HttpServlet {

  private static final long serialVersionUID = 209841279392385240L;
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException{
    doPost(request, response);
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse resp)
    throws ServletException, IOException {
    // fetch request parameters for an authorization on server
    String initURL = request.getParameter("url"),
        login = request.getParameter("username"),
          password = request.getParameter("password");
    // all parameters are mandatory
    if (isEmpty(initURL) || isEmpty(login) || isEmpty(password)){
      resp.setStatus(SC_BAD_REQUEST);
      return;
    }  
    
    // security constraint for requested resource (only within this server)
    String serverURL = getServerUrl(request);
    try {
      URL url = new URL(initURL);
      initURL = serverURL + url.getFile();
    } catch (MalformedURLException e) {
      initURL = serverURL + (initURL.startsWith("/") ? "" : "/") + initURL;
    }
    
    // if the user was authorized, execute logout before login procedure
    if (request.getUserPrincipal() != null){
      request.getSession().invalidate();
      request.logout();
    }
    try {
      request.login(login, password);
    }
    catch(ServletException e){
      // incorrect authentication data
      e.printStackTrace();
      resp.sendError(SC_UNAUTHORIZED);
      return;
    }
    resp.sendRedirect(resp.encodeRedirectURL(initURL));
  }
  
  /**
   * Копия метода {@link JepServerUtil#getServerUrl} для обеспечения автономности
   * данного класса от JepServerUtil
   */
  public static String getServerUrl(HttpServletRequest request) {
    return request.getScheme().toLowerCase()
        + "://"
        + request.getServerName()
        + ":"
        + request.getServerPort();
  }
}

