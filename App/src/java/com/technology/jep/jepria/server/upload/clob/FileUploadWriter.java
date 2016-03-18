package com.technology.jep.jepria.server.upload.clob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.Map;

import com.technology.jep.jepria.server.exceptions.SpaceException;
import com.technology.jep.jepria.server.upload.FileUpload;
import com.technology.jep.jepria.shared.exceptions.ApplicationException;
import com.technology.jep.jepria.shared.exceptions.NotImplementedYetException;
import com.technology.jep.jepria.shared.exceptions.SystemException;

/**
 * <pre>
 * Writer-обёртка для бина. 
 * Используется для загрузки текстовых файлов в поля CLOB.
 * 
 * Пример использования:
 * 
 * // Получим объект, реализующий интерфейс FileUpload
 * TextFileUploadLocal upload = (TextFileUploadLocal) new InitialContext().lookup(UPLOAD_BEAN_JNDI_NAME);
 * 
 * // Передаем в метод поток для чтения из файла, объект upload,
 * // имя таблицы, имя поля LOB, имя ключевого поля, значение ключа
 * // в таблице, имя источника данных, имя ресурса.
 * FileUploadWriter.uploadFile(
 *				stream
 *				, upload
 *				, TABLE_NAME
 *				, LOB_FIELD_NAME
 *				, KEY_FIELD_NAME
 *				, new BigDecimal(loadTaskId.intValue())
 *				, DATA_SOURCE_JNDI_NAME
 *				, RESOURCE_BUNDLE_NAME);
 * </pre>
 */
public class FileUploadWriter extends Writer {
    
	/**
	 * Интерфейс бина, который будет использоваться для записи файла в базу данных.
	 */
	private TextFileUpload fileUpload;

	/**
	 * Конструктор.
	 *
	 * @param fileUpload	интерфейс бина, который будет использоваться для записи файла в базу данных.
	 */	    
	public FileUploadWriter(TextFileUpload fileUpload) throws RemoteException, ApplicationException {
		if (fileUpload == null) {
			throw new IllegalArgumentException("The bean reference is not expected to be null");
		}
		this.fileUpload = fileUpload;
	}

	/**
	 * Закрывает выходной поток. Пустая реализация на текущий момент.
	 */	    
	public void close() throws IOException {

	}

	/**
	 * Очищает выходной поток и записывает все буферизированные данные. 
	 * На текущий момент вызывает исключение UnsupportedOperationException("Not impemented yet").
	 */	    
	public void flush() throws IOException {
		throw new UnsupportedOperationException("Not impemented yet");
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		try {
			fileUpload.continueWrite(cbuf);
		} catch (Exception e) {
			throw new RuntimeException("Exception ocuccured on the remote side", e);
		}
	}
	
	/**
	 * Загрузка файла из входного потока в поле Clob таблицы базы данных.
	 * 
	 * @param reader     					входной поток чтения файла
	 * @param fileUpload					интерфейс выгрузки файла
	 * @param tableName     			имя таблицы, откуда берем СLOB
	 * @param fileFieldName  			имя атрибута в таблице, откуда берем СLOB
	 * @param keyFieldName  			PK в таблице tableName
	 * @param rowId 							идентификатор строки таблицы
	 * @param dataSourceJndiName 	имя источника данных
	 * @param resourceBundleName 	имя ресурсов
	 * @throws IOException
	 */
	public static void uploadFile(
			Reader reader
			, FileUpload fileUpload
			, String tableName
			, String fileFieldName
			, String keyFieldName
			, Object rowId
			, String dataSourceJndiName) 
			throws IOException {

		Writer writeStream = null;
		BufferedReader bufferedReader = null;
		try {
			// Здесь выполняется преобразование из байтов в символы
			bufferedReader = new BufferedReader(reader);
			final int WRITE_LENGTH = fileUpload.beginWrite(
					tableName
					, fileFieldName
					, keyFieldName
					, rowId
					, dataSourceJndiName);
			writeStream = new FileUploadWriter((TextFileUpload)fileUpload);
			char[] readBuffer = new char[WRITE_LENGTH];
			while (true) {
				int size = bufferedReader.read(readBuffer);
				if (size == -1) {
					break;
				} else if (size == WRITE_LENGTH) {
					writeStream.write(readBuffer);
				} else {
					char[] lastBuffer = new char[size];
					System.arraycopy(readBuffer, 0, lastBuffer, 0, size);
					writeStream.write(lastBuffer);
				}
			}
			
		} catch (Exception e) {
			throw new SystemException(e.getMessage(), e);
		} finally {
			if(bufferedReader != null) { 
				bufferedReader.close();
			}
			if(writeStream != null) {
				writeStream.close();
			}
			try {
				fileUpload.endWrite();
			} catch (SpaceException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Загрузка файла из входного потока в поле Clob таблицы базы данных по сложному первичному ключу.
	 * 
	 * @param reader     					входной поток чтения файла
	 * @param fileUpload					интерфейс выгрузки файла
	 * @param tableName     			имя таблицы, откуда берем СLOB
	 * @param fileFieldName  			имя атрибута в таблице, откуда берем СLOB
	 * @param primaryKeyMap  			PK в таблице tableName
	 * @param dataSourceJndiName 	имя источника данных
	 */
	public static void uploadFile(
			Reader reader
			, FileUpload fileUpload
			, String tableName
			, String fileFieldName
			, Map<String, Object> primaryKeyMap
			, String dataSourceJndiName) {
		throw new NotImplementedYetException();
	}

}
