package com.technology.jep.jepria.client.entrance;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.technology.jep.jepria.shared.service.JepMainServiceAsync;

/**
 * TODO Напрашивается переименование
 * 
 * Класс обработки Logout
 */
public class Entrance {

  private static JepMainServiceAsync mainService = null;
  
  public static void setService(JepMainServiceAsync service) {
    mainService = service;
  }
  
  /**
   * Выход из приложения
   */
  public static void logout() {
    mainService.logout(getLocation(), new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        reload();
      }

      public void onSuccess(String logoutUrl) {
        if(logoutUrl != null) {
          goTo(logoutUrl);
        } else {
          reload();
        }
      }
    });
  }

  public native static String getLocation()/*-{
      if ($wnd.parent) {
        return $wnd.parent.location.href;
      } else {
        return $wnd.location.href;
      }
  }-*/;

  /**
   * Перезагрузка страницы (с учётом окружения - с Navigation или без)
   */
  public native static void reload() /*-{
    try {
      $wnd.parent.location.reload(true); // Сначала пробуем reload для фреймовой конфигурации
    } catch(error) {
      $wnd.location.reload(true); // На фреймах не сработало, значит у нас standalone-страница
    }
  }-*/;

  /**
   * Переход по заданному Url
   * 
   * @param url
   */
  public native static void goTo(String url) /*-{
    if ($wnd.parent) {
      $wnd.parent.location.href = url;
    } else {
      $wnd.location.href = url;
    }
  }-*/;
}
