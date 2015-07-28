package com.technology.jep.jepria.server.service;

import static com.technology.jep.jepria.server.JepRiaServerConstant.BINARY_FILE_DOWNLOAD_BEAN_JNDI_NAME;
import static com.technology.jep.jepria.server.JepRiaServerConstant.EXCEL_REPORT_FIELDS_SESSION_ATTRIBUTE;
import static com.technology.jep.jepria.server.JepRiaServerConstant.EXCEL_REPORT_HEADERS_SESSION_ATTRIBUTE;
import static com.technology.jep.jepria.server.JepRiaServerConstant.FOUND_RECORDS_SESSION_ATTRIBUTE;
import static com.technology.jep.jepria.server.JepRiaServerConstant.IS_REFRESH_NEEDED;
import static com.technology.jep.jepria.server.JepRiaServerConstant.SELECTED_RECORDS_SESSION_ATTRIBUTE;
import static com.technology.jep.jepria.server.JepRiaServerConstant.TEXT_FILE_UPLOAD_BEAN_JNDI_NAME;
import static com.technology.jep.jepria.shared.JepRiaConstant.*;
import static com.technology.jep.jepria.shared.field.JepFieldNames.MAX_ROW_COUNT;
import static com.technology.jep.jepria.shared.field.JepTypeEnum.BINARY_FILE;
import static com.technology.jep.jepria.shared.field.JepTypeEnum.CLOB;
import static com.technology.jep.jepria.shared.field.JepTypeEnum.STRING;
import static com.technology.jep.jepria.shared.field.JepTypeEnum.TEXT_FILE;
import static com.technology.jep.jepria.shared.load.PagingConfig.DEFAULT_PAGE_SIZE;
import static com.technology.jep.jepria.shared.util.JepRiaUtil.isEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.technology.jep.jepria.server.download.blob.BinaryFileDownloadLocal;
import com.technology.jep.jepria.server.download.blob.FileDownloadStream;
import com.technology.jep.jepria.server.ejb.JepDataStandard;
import com.technology.jep.jepria.server.upload.clob.FileUploadWriter;
import com.technology.jep.jepria.server.upload.clob.TextFileUploadLocal;
import com.technology.jep.jepria.server.util.JepServerUtil;
import com.technology.jep.jepria.shared.dto.JepSorter;
import com.technology.jep.jepria.shared.exceptions.SystemException;
import com.technology.jep.jepria.shared.field.JepLikeEnum;
import com.technology.jep.jepria.shared.field.JepTypeEnum;
import com.technology.jep.jepria.shared.load.FindConfig;
import com.technology.jep.jepria.shared.load.PagingConfig;
import com.technology.jep.jepria.shared.load.PagingResult;
import com.technology.jep.jepria.shared.load.SortConfig;
import com.technology.jep.jepria.shared.load.SortConfig.SortDir;
import com.technology.jep.jepria.shared.record.JepRecord;
import com.technology.jep.jepria.shared.record.JepRecordDefinition;
import com.technology.jep.jepria.shared.record.lob.JepFileReference;
import com.technology.jep.jepria.shared.record.lob.JepLobRecordDefinition;
import com.technology.jep.jepria.shared.service.data.JepDataService;
import com.technology.jep.jepria.shared.util.Mutable;
import com.technology.jep.jepria.shared.util.SimplePaging;

/**
 * Абстрактный предок сервисов данных Jep
 */
@SuppressWarnings("serial")
abstract public class JepDataServiceServlet extends JepServiceServlet implements JepDataService {
	protected static Logger logger = Logger.getLogger(JepDataServiceServlet.class.getName());	
	
	protected JepRecordDefinition recordDefinition = null;
	protected JepSorter sorter = null;
	protected String ejbName = null;
	protected String dataSourceJndiName = null;
	protected String resourceBundleName = null;
	
	protected JepDataServiceServlet(JepRecordDefinition recordDefinition, String ejbName) {
		this.recordDefinition = recordDefinition;
		this.sorter = new JepSorter<JepRecord>();
		this.ejbName = ejbName;
	}
	
	/**
	 * Конструктор необходим при использовании download 
	 * 
	 * @param recordDefinition
	 * @param ejbName
	 * @param dataSourceJndiName
	 * @param resourceBundleName
	 */
	protected JepDataServiceServlet(
			JepRecordDefinition recordDefinition,
			String ejbName,
			String dataSourceJndiName,
			String resourceBundleName) {
		this(recordDefinition, ejbName);
		this.dataSourceJndiName = dataSourceJndiName;
		this.resourceBundleName = resourceBundleName;
	}

	public JepRecord update(FindConfig updateConfig) {
		JepRecord record = updateConfig.getTemplateRecord();
		
		logger.trace("BEGIN update(" + record + ")");
		JepRecord resultRecord = null;
		
		prepareFileFields(record);
		
		try {
			JepDataStandard ejb = (JepDataStandard) JepServerUtil.ejbLookup(ejbName);
			ejb.update(record, getOperatorId());
			updateLobFields(record);
			resultRecord = findByPrimaryKey(recordDefinition.buildPrimaryKeyMap(record));
			clearFoundRecords(updateConfig);
		} catch (Throwable th) {
			String message = "Update error";
			logger.error(message, th);
			throw buildException(message, th);
		}

		logger.trace("END update(" + resultRecord + ")");
		return resultRecord;
	}

	public JepRecord create(FindConfig createConfig) {
		JepRecord record = createConfig.getTemplateRecord();
		
		logger.trace("BEGIN create(" + record + ")");
		JepRecord resultRecord = null;

		prepareFileFields(record);
		
		try {
			JepDataStandard ejb = (JepDataStandard) JepServerUtil.ejbLookup(ejbName);
			Object recordId = ejb.create(record, getOperatorId());
			String[] primaryKey = recordDefinition.getPrimaryKey();
			if(recordId != null) {
				if(primaryKey.length == 1) {
					record.set(primaryKey[0], recordId); // TODO Разобраться со случаями (очень частыми), когда pk уже присутствует
				} else {
					throw new SystemException("When create return non-null, primary key should be simple, but detected: primaryKey = " + primaryKey);
				}
			}
			updateLobFields(record);
			resultRecord = findByPrimaryKey(recordDefinition.buildPrimaryKeyMap(record));
			clearFoundRecords(createConfig);
		} catch (Throwable th) {
			String message = "Create error";
			logger.error(message, th);
			throw buildException(message, th);
		}
		
		logger.trace("END create(" + record + ")");
		return resultRecord;
	}

	protected JepRecord findByPrimaryKey(Map<String, Object> primaryKey) {
		logger.trace("BEGIN findByPrimaryKey(" + primaryKey + ")");
		
		JepRecord templateRecord = new JepRecord();
		Set<String> keySet = primaryKey.keySet();
		for(String key: keySet) {
			templateRecord.set(key, primaryKey.get(key));
		}
		templateRecord.set(MAX_ROW_COUNT, 1);
		
		PagingConfig pagingConfig = new PagingConfig(templateRecord);
		PagingResult<JepRecord> pagingResult = find(pagingConfig);
		List<JepRecord> list = pagingResult.getData();
		
		JepRecord result = list.size() > 0 ? list.get(0) : null;
		
		logger.trace("END findByPrimaryKey(" + primaryKey + ")");
		
		return result;
	}

	public void delete(FindConfig deleteConfig) {
		JepRecord record = deleteConfig.getTemplateRecord();
		logger.trace("BEGIN delete(" + record + ")");
		
		try {
			JepDataStandard ejb = (JepDataStandard) JepServerUtil.ejbLookup(ejbName);
			ejb.delete(record, getOperatorId());
			clearFoundRecords(deleteConfig);			
		} catch (Throwable th) {
			String message = "Delete error";
			logger.error(message, th);
			throw buildException(message, th);
		}
		
		logger.trace("END delete(" + record + ")");
	}

	/**
	 * Поиск данных на основе заданной конфигурации поиска.<br/>
	 * После осуществления поиска полученный список размещается в сессии.
	 *
	 * @param pagingConfig конфигурация поиска
	 * @return результаты поиска
	 */
	public PagingResult<JepRecord> find(PagingConfig pagingConfig) {
		logger.trace("BEGIN find(" + pagingConfig + ")");
		
		PagingResult<JepRecord> pagingResult = new PagingResult<JepRecord>();
		List<JepRecord> resultRecords = null;
		JepRecord findModel = pagingConfig.getTemplateRecord();
		
		Integer maxRowCount = pagingConfig.getMaxRowCount();
		if(maxRowCount == null) {
			maxRowCount = new Integer(DEFAULT_MAX_ROW_COUNT);
		}

		prepareLikeFields(findModel);
		
		Mutable<Boolean> autoRefreshFlag = new Mutable<Boolean>(false);
		try {
			JepDataStandard ejb = (JepDataStandard) JepServerUtil.ejbLookup(ejbName);
			resultRecords = ejb.find(
					findModel,
					autoRefreshFlag,
					maxRowCount,
					getOperatorId());
		} catch (Throwable th) {
			String message = "Find error";
			logger.error(message, th);
			throw buildException(message, th);
		}
		
		HttpSession session = getThreadLocalRequest().getSession();
		// Сохраним результаты поиска для возможного повторного использования в приложении 
		// (например, для сортировки или выгрузки отчета в Excel).
		session.setAttribute(FOUND_RECORDS_SESSION_ATTRIBUTE + pagingConfig.getListUID(), resultRecords);
		
		// Запишем в сессию флаг автообновления.
		session.setAttribute(IS_REFRESH_NEEDED + pagingConfig.getListUID(), autoRefreshFlag.get());

		// Поддержка функционала листания полученного набора данных.
		Integer pageSize = pagingConfig.getPageSize();
		pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;

		SimplePaging simplePaging = new SimplePaging(resultRecords, pageSize);

		pagingResult.setSize(resultRecords.size());
		pagingResult.setData(simplePaging.getPage(1));
		pagingResult.setPageSize(simplePaging.getPageSize());
		pagingResult.setActivePage(simplePaging.getPageIndex());
		
		logger.trace("END find(" + pagingConfig + ")");
		return pagingResult;
	}
	
	/**
	 * Сортировка сохраненного в сессии списка.<br/>
	 * При отсутствии в сессии запрошенного списка, выполняется повторный поиск 
	 * {@link com.technology.jep.jepria.server.service.JepDataServiceServlet#find(PagingConfig pagingConfig)}.
	 *
	 * @param sortConfig конфигурация сортировки
	 * @return результат сортировки
	 */
	@SuppressWarnings("unchecked")
	public PagingResult<JepRecord> sort(SortConfig sortConfig) {
		logger.trace("BEGIN sort(" + sortConfig + ")");
		
		PagingResult<JepRecord> pagingResult = new PagingResult<JepRecord>();
		List<JepRecord> resultRecords = null;
		
		HttpSession session = getThreadLocalRequest().getSession();
		resultRecords = (List<JepRecord>)session.getAttribute(FOUND_RECORDS_SESSION_ATTRIBUTE + sortConfig.getListUID());
		
		// Если в сессии не оказалось необходимых данных, то получим их заново.
		if(resultRecords == null) {
			find(sortConfig);
			resultRecords = (List<JepRecord>)session.getAttribute(FOUND_RECORDS_SESSION_ATTRIBUTE + sortConfig.getListUID());
		}
		
		final String sortField = sortConfig.getSortField();
		SortDir sortDir = sortConfig.getSortDir();
		
		if(sortDir == SortDir.ASC) {
			Collections.sort(resultRecords, new Comparator<JepRecord>() {
				public int compare(JepRecord m1, JepRecord m2) {
					return sorter.compare(m1, m2, sortField);
				}
			});
		} else if(sortDir == SortDir.DESC) {
			Collections.sort(resultRecords, new Comparator<JepRecord>() {
				public int compare(JepRecord m1, JepRecord m2) {
					return sorter.compare(m2, m1, sortField);
				}
			});
		}
		
		// Поддержка функционала листания полученного набора данных.
		Integer pageSize = sortConfig.getPageSize();
		pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;

		SimplePaging simplePaging = new SimplePaging(resultRecords, pageSize);

		pagingResult.setSize(resultRecords.size());
		pagingResult.setData(simplePaging.getPage(1));
		pagingResult.setPageSize(simplePaging.getPageSize());
		pagingResult.setActivePage(simplePaging.getPageIndex());
		
		logger.trace("END sort(" + sortConfig + ")");
		return pagingResult;
	}
	
	/**
	 * Листание сохраненного в сессии списка.<br/>
	 * При отсутствии в сессии запрошенного списка, выполняется повторный поиск 
	 * {@link com.technology.jep.jepria.server.service.JepDataServiceServlet#find(PagingConfig pagingConfig)}.
	 *
	 * @param pagingConfig конфигурация листания
	 * @return результат листания
	 */
	@SuppressWarnings("unchecked")
	public PagingResult<JepRecord> paging(PagingConfig pagingConfig) {
		logger.trace("BEGIN paging(" + pagingConfig + ")");
		
		PagingResult<JepRecord> pagingResult = new PagingResult<JepRecord>();
		List<JepRecord> resultRecords = null;
		
		HttpSession session = getThreadLocalRequest().getSession();
		resultRecords = (List<JepRecord>)session.getAttribute(FOUND_RECORDS_SESSION_ATTRIBUTE + pagingConfig.getListUID());
		
		// Если в сессии не оказалось необходимых данных, то получим их заново.
		if(resultRecords == null) {
			find(pagingConfig);
			resultRecords = (List<JepRecord>)session.getAttribute(FOUND_RECORDS_SESSION_ATTRIBUTE + pagingConfig.getListUID());
		}
		
		// Поддержка функционала листания полученного набора данных.
		Integer pageSize = pagingConfig.getPageSize();
		pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;

		SimplePaging simplePaging = new SimplePaging(resultRecords, pageSize);
		
		Integer activePage = pagingConfig.getActivePage();
		activePage = activePage == null ? 1 : activePage;

		pagingResult.setSize(resultRecords.size());
		pagingResult.setData(simplePaging.getPage(activePage));
		pagingResult.setPageSize(simplePaging.getPageSize());
		pagingResult.setActivePage(simplePaging.getPageIndex());
		
		logger.trace("END paging(" + pagingConfig + ")");
		return pagingResult;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer prepareDownload(String fileName, String mimeType, String fieldName, String recordKey) {
		return prepareDownload(fileName, mimeType, fieldName, recordKey, DOWNLOAD_CONTENT_DISPOSITION_ATTACHMENT, null, null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer prepareDownload(String fileName, String mimeType, String fieldName, String recordKey, String contentDisposition, String extension, String fileNamePrefix) {
		Integer downloadId = (new Random()).nextInt();
		HttpSession session = getThreadLocalRequest().getSession();
		session.setAttribute(DOWNLOAD_FILE_NAME + downloadId, fileName);
		session.setAttribute(DOWNLOAD_MIME_TYPE + downloadId, mimeType);
		session.setAttribute(DOWNLOAD_FIELD_NAME + downloadId, fieldName);
		session.setAttribute(DOWNLOAD_RECORD_KEY + downloadId, recordKey);
		session.setAttribute(DOWNLOAD_CONTENT_DISPOSITION + downloadId, contentDisposition);
		session.setAttribute(DOWNLOAD_EXTENSION + downloadId, extension);
		session.setAttribute(DOWNLOAD_FILE_NAME_PREFIX + downloadId, fileNamePrefix);
		return downloadId;
	}
	
	/**
	 * Подготавливает данные для формирования Excel-отчета.<br/>
	 * При отсутствии в сессии набора данных для формирования отчета - выполняется повторный поиск 
	 * {@link com.technology.jep.jepria.server.service.JepDataServiceServlet#find(PagingConfig pagingConfig)}.
	 *
	 * @param pagingConfig параметры поиска
	 * @param selectedRecords выбранные записи для формирования отчета
	 * @param reportHeaders список содержащий названия колонок
	 * @param reportFields список содержащий идентификаторы полей, из которых брать данные для колонок
	 */
	@SuppressWarnings("unchecked")
	public void prepareExcel(PagingConfig pagingConfig, List<JepRecord> selectedRecords, List<String> reportHeaders, List<String> reportFields) {
		logger.trace("BEGIN prepareExcel(" + pagingConfig + ")");

		Integer listUID = pagingConfig.getListUID();
		HttpSession session = getThreadLocalRequest().getSession();

		// Если в selectedRecords только одна строка - работаем по полному набору данных!
		if (selectedRecords != null && selectedRecords.size() < 2) {
			selectedRecords = null;
		}
		
		session.setAttribute(SELECTED_RECORDS_SESSION_ATTRIBUTE + listUID, selectedRecords);
		List<JepRecord> resultRecords = (List<JepRecord>)session.getAttribute(FOUND_RECORDS_SESSION_ATTRIBUTE + listUID);

		// Если в сессии не оказалось необходимых данных, то получим их заново.
		if(selectedRecords == null && resultRecords == null) {
			find(pagingConfig);
		}

		session.setAttribute(EXCEL_REPORT_HEADERS_SESSION_ATTRIBUTE + listUID, reportHeaders);
		session.setAttribute(EXCEL_REPORT_FIELDS_SESSION_ATTRIBUTE + listUID, reportFields);

		logger.trace("END prepareExcel(" + pagingConfig + ")");
	}
	
	/**
	 * Получение выходного потока из заданного Lob-поля
	 * Используется наследниками (классами приложений) для получения значений blob-полей
	 * 
	 * @param tableName
	 * @param fileFieldName
	 * @param keyFieldName
	 * @param recordId
	 * @return выходной поток
	 */
	protected OutputStream getBinaryOutputStream(
			String tableName,
			String fileFieldName,
			String keyFieldName,
			Integer recordId) {
		
		logger.trace("BEGIN getBinaryOutputStream(" + tableName + ", " + fileFieldName + ", " + keyFieldName + ", " + recordId + ")");
		OutputStream result = null;
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			FileDownloadStream.downloadFile(
					outputStream,
					(BinaryFileDownloadLocal) JepServerUtil.ejbLookup(BINARY_FILE_DOWNLOAD_BEAN_JNDI_NAME),
					tableName,
					fileFieldName,
					keyFieldName,
					recordId,
					this.dataSourceJndiName,
					this.resourceBundleName);

			result = outputStream;
		} catch (Throwable th) {
			logger.error("getBinaryOutputStream() error", th);
			throw new SystemException(th.getMessage(), th);
		}
		
		logger.trace("BEGIN getBinaryOutputStream(" + tableName + ", " + fileFieldName + ", " + keyFieldName + ", " + recordId + ")");
		return result;
	}
	
	/**
	 * Заполнение полей расширений имен файлов на основе имен файлов (передается как строковое значение в Lob-поле).
	 * Заполнение mime-type полей на основе значений полей расширений имен файлов.
	 */
	protected void prepareFileFields(JepRecord record) {
		if(recordDefinition instanceof JepLobRecordDefinition) {
			Map<String, String> fieldMap = ((JepLobRecordDefinition)recordDefinition).getFieldMap();
			Map<String, JepTypeEnum> typeMap = ((JepLobRecordDefinition)recordDefinition).getTypeMap();
			if(fieldMap != null && typeMap != null) {
				Set<Map.Entry<String, String>> entries = fieldMap.entrySet();
				for(Map.Entry<String, String> fieldMapEntry: entries) {
					String fieldName = fieldMapEntry.getKey();
					JepTypeEnum fieldType = typeMap.get(fieldName);
					if(fieldType == BINARY_FILE || fieldType == TEXT_FILE) {
						String fileName = JepFileReference.getFileName(record.get(fieldName));
						if(!isEmpty(fileName)) {
							String fileExtension = obtainFileExtension(fileName);
							String mimeType = JepServerUtil.detectMimeType(fileExtension);
							// Запись в поля расширений файлов (если они есть)
							String primaryKey = recordDefinition.getPrimaryKey()[0];
							Object key = record.get(primaryKey);
							JepFileReference fileReference = null;
							if (JepTypeEnum.INTEGER.equals(typeMap.get(primaryKey))) {
								fileReference = new JepFileReference(fileName, (Integer)key, fileExtension, mimeType);
							}
							else {
								fileReference = new JepFileReference(fileName, (String)key, fileExtension, mimeType);
							}
							record.set(fieldName, fileReference);
						}
					}
				}
			}
		}
	}

	private String obtainFileExtension(String fileName) {
		String result = null;
		int lastDotIndex = fileName.lastIndexOf('.');
		if(lastDotIndex >= 0 && (lastDotIndex + 1) < fileName.length()) {
			result = fileName.substring(lastDotIndex + 1);
		}
		return result;
	}

	/**
	 * Добавление управляющих символов к значениям текстовых полей для поиска.
	 */
	protected void prepareLikeFields(JepRecord record) {
		Map<String, JepLikeEnum> likeMap = recordDefinition.getLikeMap();
		Map<String, JepTypeEnum> typeMap = recordDefinition.getTypeMap();
		if(likeMap != null && typeMap != null) {
			Set<Map.Entry<String, JepLikeEnum>> entries = likeMap.entrySet();
			for(Map.Entry<String, JepLikeEnum> likeMapEntry: entries) {
				String fieldName = likeMapEntry.getKey();
				JepTypeEnum fieldType = typeMap.get(fieldName);
				if(fieldType == STRING) {
					Object fieldValueObject = record.get(fieldName);
					String fieldValue = fieldValueObject instanceof String ? (String)fieldValueObject : null;
					if(!isEmpty(fieldValue)) {
						JepLikeEnum like = likeMapEntry.getValue();
						switch(like) {
							case FIRST:
								fieldValue = fieldValue + "%";
								break;
							case CONTAINS:
								fieldValue = "%" + fieldValue + "%";
								break;
							case LAST:
								fieldValue = "%" + fieldValue;
								break;
						}
						record.set(fieldName, fieldValue);
					}
				}
			}
		}
	}

	/**
	 * Обновление Lob-полей в базе данных.
	 */
	protected void updateLobFields(JepRecord record) throws IOException, Exception {
		if(recordDefinition instanceof JepLobRecordDefinition) {
			Map<String, Object> primaryKeyMap = recordDefinition.buildPrimaryKeyMap(record);
			String tableName = ((JepLobRecordDefinition)recordDefinition).getTableName();
			Map<String, String> fieldMap = ((JepLobRecordDefinition)recordDefinition).getFieldMap();
			Map<String, JepTypeEnum> typeMap = ((JepLobRecordDefinition)recordDefinition).getTypeMap();
			if(fieldMap != null && typeMap != null) {
				Set<Map.Entry<String, String>> entries = fieldMap.entrySet();
				for(Map.Entry<String, String> fieldMapEntry: entries) {
					String fieldName = fieldMapEntry.getKey();
					String fileFieldName = fieldMapEntry.getValue();
					JepTypeEnum fieldType = typeMap.get(fieldName);
					if(fieldType == CLOB) {
						Object textObject = record.get(fieldName);
						String text = textObject instanceof String ? (String)textObject : null;
						text = isEmpty(text) ? " " : text; // "Стираем" текущее значение поля, если передано пустое значение.
						upload(text, tableName, fileFieldName, primaryKeyMap);
					}
				}
			}
		}
	}

	private void upload(String text,
						String tableName,
						String fileFieldName,
						Map<String, Object> primaryKeyMap) throws IOException, Exception {
		Reader reader = new StringReader(text);
		
		if(primaryKeyMap.size() == 1) {
			FileUploadWriter.uploadFile(
					reader,
					(TextFileUploadLocal) JepServerUtil.ejbLookup(TEXT_FILE_UPLOAD_BEAN_JNDI_NAME),
					tableName,
					fileFieldName,
					((JepLobRecordDefinition)recordDefinition).getKeyFieldName(),
					primaryKeyMap.values().toArray()[0],
					this.dataSourceJndiName,
					this.resourceBundleName);
		} else {
			FileUploadWriter.uploadFile(
					reader,
					(TextFileUploadLocal) JepServerUtil.ejbLookup(TEXT_FILE_UPLOAD_BEAN_JNDI_NAME),
					tableName,
					fileFieldName,
					primaryKeyMap,
					this.dataSourceJndiName,
					this.resourceBundleName);
		}
	}
	
	/**
	 * Удаление из сессии набора записей соответствующих заданной конфигурации поиска.
	 * 
	 * @param findConfig конфигурация поиска
	 */
	private void clearFoundRecords(FindConfig findConfig) {
		HttpSession session = getThreadLocalRequest().getSession();
		session.removeAttribute(FOUND_RECORDS_SESSION_ATTRIBUTE + findConfig.getListUID());
	}

	/**
	 * Извлекает из сессии флаг автообновления.
	 * @param listUID уникальный идентификатор списка
	 * @return TRUE, если автообновление нужно; FALSE в противном случае
	 */
	public Boolean isRefreshNeeded(Integer listUID) {
		HttpSession session = getThreadLocalRequest().getSession();
		return Boolean.TRUE.equals(session.getAttribute(IS_REFRESH_NEEDED + listUID));
	}
}
