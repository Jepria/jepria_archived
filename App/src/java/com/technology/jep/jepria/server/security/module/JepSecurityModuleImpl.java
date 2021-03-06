package com.technology.jep.jepria.server.security.module;

import com.technology.jep.jepcommon.security.pkg_Operator;
import com.technology.jep.jepria.server.security.OAuthRequestWrapper;
import org.apache.log4j.Logger;
import org.jepria.oauth.sdk.State;
import org.jepria.server.env.EnvironmentPropertySupport;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.net.URL;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Objects;

import static com.technology.jep.jepria.server.security.JepSecurityConstant.*;
import static org.jepria.oauth.sdk.OAuthConstants.*;

/**
 * Модуль поддержки безопасности для Tomcat
 * TODO Убрать избыточный код из аналогов
 */
public class JepSecurityModuleImpl extends JepAbstractSecurityModule {

  private static String moduleName;
  private static String clientId;

  static {
    logger = Logger.getLogger(JepSecurityModuleImpl.class.getName());
  }

  /**
   * Возвращает объект типа JepSecurityModule из сессии. Если объект не
   * найден в сессии или устаревший (например, оставшийся в сессии модуля после logout()),
   * то создается новый объект и помещается в сессию.
   * 
   * @param request запрос, из которого получим сессию
   * @return объект типа JepSecurityModule из сессии
   * TODO Попробовать уменьшить размер синхронизируемого кода (synchronized). Точно ли нужна синхронизация ?
   */
  public static synchronized JepSecurityModule getInstance(HttpServletRequest request) {
    HttpSession session = request.getSession();
    Principal principal = request.getUserPrincipal();
    JepSecurityModuleImpl securityModule;
    moduleName = request.getServletContext().getContextPath().replaceFirst("/", "");
    clientId = EnvironmentPropertySupport.getInstance(request).getProperty(moduleName + "/" + CLIENT_ID_PROPERTY);
    if(principal == null) { // Работает гость ?
      securityModule = (JepSecurityModuleImpl) session.getAttribute(JEP_SECURITY_MODULE_ATTRIBUTE_NAME);
      if(securityModule == null) { // Первый вход ?
        securityModule = new JepSecurityModuleImpl();
        session.setAttribute(JEP_SECURITY_MODULE_ATTRIBUTE_NAME, securityModule);
        securityModule.doLogonByGuest();
      }
    } else {  // Входили через SSO
      securityModule = (JepSecurityModuleImpl) session.getAttribute(JEP_SECURITY_MODULE_ATTRIBUTE_NAME);
      if (securityModule == null || securityModule.isObsolete(principal)) {
        securityModule = new JepSecurityModuleImpl();
        session.setAttribute(JEP_SECURITY_MODULE_ATTRIBUTE_NAME, securityModule);
        securityModule.updateSubject(principal);
      }
    }
    return securityModule;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String logout(HttpServletRequest request, HttpServletResponse response, String currentUrl) throws Exception {
    logger.info(this.getClass() + ".logout(request, response, " + currentUrl + ")");
    if (request instanceof OAuthRequestWrapper) {
      URL url = URI.create(currentUrl).toURL();
      State state = new State(url.getQuery() + "#" + url.getRef());
      Cookie stateCookie = new Cookie(OAUTH_CSRF_TOKEN, state.toString());
      stateCookie.setPath(request.getContextPath());
      stateCookie.setHttpOnly(true);
      response.addCookie(stateCookie);
      String hostUrl = url.getProtocol() + "://" + url.getHost() + (url.getPort() != -1 ? (":" + url.getPort()) : "");
      if (url.getPath().startsWith(request.getContextPath())) {
        currentUrl = hostUrl + OAUTH_LOGOUT_CONTEXT_PATH + "?"
          + "&" + CLIENT_ID + "=" + clientId
          + "&" + REDIRECT_URI + "="
          + Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString((hostUrl + url.getPath()).getBytes())
          + "&" + STATE + "=" + state.toString();
      } else {
        currentUrl = hostUrl + OAUTH_LOGOUT_CONTEXT_PATH + "?"
          + "&" + CLIENT_ID + "=" + EnvironmentPropertySupport
            .getInstance(request)
            .getProperty(url.getPath().replaceFirst("/", "") + CLIENT_ID_PROPERTY)
          + "&" + REDIRECT_URI + "="
          + Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString((hostUrl + url.getPath()).getBytes())
          + "&" + STATE + "=" + state.toString();
      }
    }
    request.getSession().invalidate();
    request.logout();
    return currentUrl;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Integer getJepPrincipalOperatorId(Principal principal) {
    Integer result = null;
    try {
      if(isObsolete(principal)) { // Обновить свойства, если изменился информация об операторе
        updateSubject(principal);
      }
      result = operatorId;
    } finally {
      db.closeAll(); // освобождение соединения, берущегося в logon->db.prepare
    }

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void updateSubject(Principal principal) {
    logger.trace(this.getClass() + ".updateSubject() BEGIN");
    String principalName = principal.getName();
    logger.trace("principalName = " + principalName);
    this.username = principalName;

    isAuthorizedBySso = principal != null;
    
    try {
      roles = pkg_Operator.getRoles(db, principalName);
      Integer logonOperatorId = pkg_Operator.logon(db, principalName);
      if(logonOperatorId != null) {
        operatorId = logonOperatorId;
      }
    } catch (SQLException ex) {
      logger.error("pkg_Operator error", ex);
    } finally {
      db.closeAll(); // освобождение соединения, берущегося в logon->db.prepare
    }
    
    logger.trace(this.getClass() + ".updateSubject() END");
  }

  /**
   * Проверка "свежести" объекта securityModule, закешированного в Http-сессии
   * Выполняется на основе сравнения значений operatorId principal-а и объекта jepSecurityModule. 
   * 
   * @param principal принципал
   * @return true, если объект jepSecurityModule устарел, иначе - false
   */
  protected boolean isObsolete(Principal principal) {
    return !Objects.equals(this.username, principal == null ? null : principal.getName());
  }
}
