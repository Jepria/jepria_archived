package org.jepria.server.service.rest;

import org.jepria.server.service.security.Credential;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Контроллер поиска объектов сущности.
 * <br/>
 * Интерфейс предусматривает только методы создания и чтения (create/POST, read/GET).
 * Методы модификации и удаления (update/PUT, delete/DELETE &mdash; стратегия обновления, переиспользования и очистки созданных объектов) 
 * определяются конкретной реализацией
 */
public interface ResourceSearchController {

  /**
   * Интерфейс клиентского поискового запроса для использования в контроллере (internal representation)
   */
  public interface SearchRequest {

    /**
     * @return оригинальный поисковый шаблон
     */
    Object getTemplate();

    /**
     * @return поисковый шаблон в виде строкового токена, используемый для сравнения двух шаблонов на equals (сравнение оригинальных объектов может быть ненадёжным)
     */
    String getTemplateToken();

    /**
     * @return упорядоченная последовательность имён столбцов списка с указанием порядка сортировки данных в каждом столбце (>=0 или <0)
     */
    LinkedHashMap<String, Integer> getListSortConfig();
  }
  
  /**
   * 
   * @param searchRequest
   * @return non-null
   */
  String postSearchRequest(SearchRequest searchRequest, Credential credential);
  /**
   * 
   * @param searchId
   * @return non-null
   * @throws NoSuchElementException if the requested searchId does not exist 
   */
  SearchRequest getSearchRequest(String searchId, Credential credential) throws NoSuchElementException;

  /**
   * 
   * @param searchId
   * @param pageSize
   * @param page
   * @return
   * @throws NoSuchElementException if the requested searchId does not exist
   */
  List<?> getResultsetPaged(String searchId, int pageSize, int page, Credential credential) throws NoSuchElementException;

  /**
   * 
   * @param searchId
   * @param credential
   * @return
   * @throws NoSuchElementException if the requested searchId does not exist
   */
  List<?> getResultset(String searchId, Credential credential) throws NoSuchElementException;

  /**
   * @param searchId
   * @return
   * @throws NoSuchElementException if the requested searchId does not exist
   */
  int getResultsetSize(String searchId, Credential credential) throws NoSuchElementException;
}
