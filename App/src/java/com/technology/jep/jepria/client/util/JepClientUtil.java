package com.technology.jep.jepria.client.util;

import static com.technology.jep.jepria.shared.JepRiaConstant.LOCAL_LANG;

import java.util.Map;
import java.util.Stack;

import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Window;

import com.technology.jep.jepria.client.history.place.JepCreatePlace;
import com.technology.jep.jepria.client.history.place.JepEditPlace;
import com.technology.jep.jepria.client.history.place.JepSearchPlace;
import com.technology.jep.jepria.client.history.place.JepSelectedPlace;
import com.technology.jep.jepria.client.history.place.JepViewDetailPlace;
import com.technology.jep.jepria.client.history.place.JepViewListPlace;
import com.technology.jep.jepria.client.history.place.JepWorkstatePlace;
import com.technology.jep.jepria.client.history.scope.JepScope;
import com.technology.jep.jepria.client.history.scope.JepScopeStack;
import com.technology.jep.jepria.client.ui.WorkstateEnum;
import com.technology.jep.jepria.client.widget.LoadingPanel;
import com.technology.jep.jepria.client.widget.field.ComboBox;
import com.technology.jep.jepria.client.widget.list.header.menu.GridHeaderMenuBar;
import com.technology.jep.jepria.shared.log.JepLoggerImpl;
import com.technology.jep.jepria.shared.record.JepRecord;
import com.technology.jep.jepria.shared.util.JepRiaUtil;

public class JepClientUtil {

  /**
   * Панель загрузки
   */
  private static final LoadingPanel loadingPanel = new LoadingPanel(); 

  public static final BodyElement BODY = Document.get().getBody();

  /**
   * Показ загрузочной панели с предустановленным заголовком и сообщением.
   *
   * @param header заголовок (меняется, если он установлен)
   * @param message описательное сообщение (меняется, если он установлен)
   */
  public static void showLoadingPanel(String header, String message) {
    loadingPanel.setHeader(header);
    loadingPanel.setMessage(message);
    loadingPanel.show();
  }

  /**
   * Показ загрузочной панели с предустановленным заголовком и сообщением. <br/>
   * 
   * Метод устарел, так как layerId больше не используется. Необходимо использовать {@link #showLoadingPanel(String, String)}
   *
   * @param layerId    идентификатор слоя блокировки
   * @param header    заголовок (меняется, если он установлен)
   * @param message    описательное сообщение (меняется, если он установлен)
   */
  @Deprecated
  public static void showLoadingPanel(String layerId, String header, String message) {
    showLoadingPanel(header, message);
  }

  /**
   * Скрытие загрузочной панели.
   */
  public static void hideLoadingPanel() {
    hideLoadingPanel(null);
  }
  
  /**
   * Скрытие загрузочной панели. <br/>
   * 
   * Метод устарел, так как layerId больше не используется. Необходимо использовать {@link #hideLoadingPanel(String)}
   *
   * @param layerId    идентификатор слоя блокировки
   */
  public static void hideLoadingPanel(final String layerId) {
    loadingPanel.hide();
  }

  public static void addForeignKey(JepRecord record) {
    Stack<JepScope> stack = JepScopeStack.instance;
    for(JepScope scope: stack) {
      Map<String, Object> foreignKey = scope.getForeignKey();
      record.setProperties(foreignKey);
    }
  }

  public static JepWorkstatePlace workstateToPlace(WorkstateEnum workstate) {
    JepWorkstatePlace place = null;
    switch(workstate) {
    case CREATE:
      place = new JepCreatePlace();
      break;
    case EDIT:
      place = new JepEditPlace();
      break;
    case SEARCH:
      place = new JepSearchPlace();
      break;
    case SELECTED:
      place = new JepSelectedPlace();
      break;
    case VIEW_DETAILS:
      place = new JepViewDetailPlace();
      break;
    case VIEW_LIST:
      place = new JepViewListPlace();
      break;
    }
    return place;
  }

  /**
   * Substitutes the indexed parameters.
   *
   * @param text the text
   * @param params the parameters
   * @return the new text
   */
  public static String substitute(String text, Object... params) {
    for (int i = 0; i < params.length; i++) {
      Object p = params[i];
      if (p == null) {
        p = "";
      }
      text = text.replaceAll("\\{" + i + "}", safeRegexReplacement(p.toString()));
    }
    return text;
  }

  /**
   * Replace any \ or $ characters in the replacement string with an escaped \\
   * or \$.
   *
   * @param replacement the regular expression replacement text
   * @return null if replacement is null or the result of escaping all \ and $
   * characters
   */
  private static String safeRegexReplacement(String replacement) {
    if (replacement == null) {
      return replacement;
    }

    return replacement.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$");
  }


  /**
   * Intended to be used to pull the value out of a CSS length. If the
   * value is "auto" or "inherit", 0 will be returned.
   *
   * @param s The CSS length string to extract
   * @return The leading numeric portion of <code>s</code>, or 0 if "auto" or
   * "inherit" are passed in.
   */
  public static native double extractLengthValue(String s) /*-{
    if (s == "auto" || s == "inherit" || s == "") {
      return 0;
    } else {
// numberRegex is similar to java.lang.Number.floatRegex, but divides
// the string into a leading numeric portion followed by an arbitrary
// portion.
      var numberRegex = @com.google.gwt.user.client.ui.UIObject::numberRegex;
      if (!numberRegex) {
        numberRegex = @com.google.gwt.user.client.ui.UIObject::numberRegex =
          /^(\s*[+-]?((\d+\.?\d*)|(\.\d+))([eE][+-]?\d+)?)(.*)$/;
      }

// Extract the leading numeric portion of s
      s = s.replace(numberRegex, "$1");
      return parseFloat(s);
    }
  }-*/;

  /**
   * Проверяет: являтся ли нажатая кнопка "специальной".<br/>
   * <br/>
   * В качестве реализации используется одноименная функция параметра event, а также осуществляется проверка нажатия клавиши Ctrl.<br/>
   * Добавлена проверка на клавишу Print Screen.<br/>
   *
   * @param event событие, срабатываемое браузером
   * @return флаг является ли нажатая кнопка "специальной" или Ctrl
   */
  public static boolean isSpecialKey(DomEvent<?> event){
    int keyCode = event.getNativeEvent().getKeyCode();
    return isSpecialKey(keyCode) || event.getNativeEvent().getCtrlKey() || keyCode == 44; //Блокируем реакцию на клавишу PrintScreen, код 44
  }

  /**
   * Returns true if the key is a "special" key.
   *
   * @param k the key code
   * @return the special state
   */
  public static boolean isSpecialKey(int k) {
    return isNavKeyPress(k) || k == KeyCodes.KEY_BACKSPACE || k == KeyCodes.KEY_CTRL || k == KeyCodes.KEY_SHIFT
        || k == KeyCodes.KEY_ALT || (k >= 19 && k <= 20) || (k >= 45 && k <= 46);
  }

  /**
   * Returns true if the key is a "navigation" key.
   *
   * @param k the key code
   * @return the nav state
   */
  public static boolean isNavKeyPress(int k) {
    return (k >= 33 && k <= 40) || k == KeyCodes.KEY_ESCAPE || k == KeyCodes.KEY_ENTER || k == KeyCodes.KEY_TAB;
  }

  // needed due to GWT 2.1 changes
  /**
   * Получение кода вводимого с клавиатуры символа <br/>
   * (актуально для случаев событий onKeyUp - разные браузеры по-разному распознают код символа, в событиях же onKeyDown - коды символов приходят корректно).
   *
   * @param e срабатываемое событие
   * @return представление кода вводимого символа, приведенного к типу char
   */
  public static native char getChar(NativeEvent e) /*-{
    return e.which || e.charCode || e.keyCode || 0;
  }-*/;

  /**
   * Поддержание активного элемента списка видимым при прокручивании списка мышью.<br/>
   *
   * Если в выпадающем меню содержится такое количество опций, что пользователю приходится прокручивать список мышью,
   * то возникает необходимость выравнивать элемент по высоте так, чтобы активный элемент списка стал видимым.<br/>
   * Данная операция используется в таких виджетах, как {@link ComboBox} и {@link GridHeaderMenuBar}.
   *
   * @param elem  активный элемент, подвергаемый выравниванию
   */
  public static native void adjustToTop(Element elem) /*-{
    var left = elem.offsetLeft, top = elem.offsetTop;
    var width = elem.offsetWidth, height = elem.offsetHeight;

    if (elem.parentNode != elem.offsetParent) {
      left -= elem.parentNode.offsetLeft;
      top -= elem.parentNode.offsetTop;
    }

    var cur = elem.parentNode;
    while (cur && (cur.nodeType == 1)) {
      if (top < cur.scrollTop) {
        cur.scrollTop = top;
      }
      if (top + height > cur.scrollTop + cur.clientHeight) {
        cur.scrollTop = (top + height) - cur.clientHeight;
      }

      var offsetLeft = cur.offsetLeft, offsetTop = cur.offsetTop;
      if (cur.parentNode != cur.offsetParent) {
        offsetTop -= cur.parentNode.offsetTop;
      }

      top += offsetTop - cur.scrollTop;
      cur = cur.parentNode;
    }
  }-*/;

  /**
   * String.trim() is limited for using in all cases.
   * Particularly, it doesn't include such html-entity as &nbsp (\u00a0),
   * To solve that, add the unicode to the RegEx.
   * @param s    string to trim
   * @return  trimmed string
   */
  public static native String jsTrim(String s) /*-{
    return s.replace(/^[\s,\u00a0]+|[\s,\u00a0]+$/g, '');
  }-*/;

  /**
   * Получение значения элемента DOM с заданным идентификатором.
   * 
   * @param id идентификатор элемента в DOM
   * @return значение в виде строки: если элемент не найден - возвращается null, если элемент пустой или не найден - возвращается пустая строка
   */
  public static native String getElementValue(String id) /*-{
    var value = null;
    var elementById = $wnd.document.getElementById(id);
    if(elementById && elementById.value !== undefined) {
      value = elementById.value;
    }
    return value;
  }-*/;

  /**
   * Проверяет, является ли текущий язык основным языком для пользователей.
   * 
   * @return возвращает true, если текущий язык является основным языком для
   *         пользователей. В противном случае возвращает false.
   */
  public static boolean isLocalLang() {
    return LOCAL_LANG.equals(LocaleInfo.getCurrentLocale().getLocaleName());
  }

  /**
   * Проверяет, является ли клиентский браузер мобильным.
   *
   * @return true - клиентский барузер является мобильным, false - клиентский браузер не является мобильным
   * @see <a href="https://deviceatlas.com/blog/mobile-browser-user-agent-strings" target="_blank">https://deviceatlas.com/blog/mobile-browser-user-agent-strings</a>
   */
  public final static boolean isMobile() {
    return JepRiaUtil.isMobile(Window.Navigator.getUserAgent());
  }
  
}