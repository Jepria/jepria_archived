package com.technology.jep.jepria.shared.service.data;

import java.util.List;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.technology.jep.jepria.shared.field.option.JepOption;
import com.technology.jep.jepria.shared.load.FindConfig;
import com.technology.jep.jepria.shared.load.PagingConfig;
import com.technology.jep.jepria.shared.load.PagingResult;
import com.technology.jep.jepria.shared.load.SortConfig;
import com.technology.jep.jepria.shared.record.JepRecord;

/**
 * Асинхронный интерфейс сервиса данных.
 */
public interface JepDataServiceAsync {

	/**
	 * Создание объекта.
	 * 
	 * @param createConfig конфигурация создаваемой записи
	 * @param callback объект асинхронного обратного вызова содержащий созданную запись
	 */
	void create(FindConfig createConfig, AsyncCallback<JepRecord> callback);

	/**
	 * Удаление объекта.
	 * 
	 * @param deleteConfig конфигурация удаляемой записи
	 * @param callback объект асинхронного обратного вызова
	 */
	void delete(FindConfig deleteConfig, AsyncCallback<Void> callback);

	/**
	 * Обновление объекта.
	 * 
	 * @param updateConfig конфигурация записи с новыми значениями
	 * @param callback объект асинхронного обратного вызова содержащий обновленную запись
	 */
	void update(FindConfig updateConfig, AsyncCallback<JepRecord> callback);

	/**
	 * Поиск.
	 * 
	 * @param pagingConfig параметры поиска
	 * @param callback объект асинхронного обратного вызова содержащий результат поиска
	 */
	void find(PagingConfig pagingConfig, AsyncCallback<PagingResult<JepRecord>> callback);

	/**
	 * Сортировка.
	 * 
	 * @param sortConfig параметры сортировки
	 * @param callback объект асинхронного обратного вызова содержащий результат сортировки
	 */
	void sort(SortConfig sortConfig, AsyncCallback<PagingResult<JepRecord>> callback);

	/**
	 * Листание данных.
	 * 
	 * @param pagingConfig параметры листания данных
	 * @param callback объект асинхронного обратного вызова содержащий результат листания
	 */
	void paging(PagingConfig pagingConfig, AsyncCallback<PagingResult<JepRecord>> callback);

	/**
	 * Подготавливает данные для формирования Excel-отчета.
	 * 
	 * @param pagingConfig параметры поиска
	 * @param reportHeaders список содержащий названия колонок
	 * @param reportFields список содержащий идентификаторы полей, из которых брать данные для колонок
	 * @param callback объект асинхронного обратного вызова
	 */
	void prepareExcel(PagingConfig pagingConfig, List<JepRecord> selectedRecords, List<String> reportHeaders, List<String> reportFields, AsyncCallback<Void> callback);

	/**
	 * Метод проверки необходимости автообновления.
	 * 
	 * @param listUID уникальный идентификатор списка
	 * @param callback объект асинхронного обратного вызова, содержащий значение, 
	 * равное TRUE, если необходимо выполнить автообновление, и FALSE в противном случае
	 */
	void isRefreshNeeded(Integer listUID, AsyncCallback<Boolean> callback);
	
}
