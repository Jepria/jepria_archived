package com.technology.jep.jepria.server.dao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.SessionContext;

import oracle.jdbc.driver.OracleTypes;

import org.apache.log4j.Logger;

import com.technology.jep.jepria.server.db.Db;
import com.technology.jep.jepria.server.ejb.CallContext;
import com.technology.jep.jepria.shared.exceptions.ApplicationException;
import com.technology.jep.jepria.shared.util.JepRiaUtil;

/**
 * <pre>
 * Класс предназначен для облегчения работы с jdbc.
 * 
 * Примеры использования:
 * 
 * 1. Пример использования метода create.
 * 
 *     Integer recordId = DaoSupport.<Integer>create(sqlQuery, sessionContext, DATA_SOURCE_JNDI_NAME,
 *       RESOURCE_BUNDLE_NAME, divisionId, branchId, employeeId, periodTypeCode, periodNumber, periodYear,
 *       operatorId);
 *
 * 2. Пример использования метода execute.
 * 
 *     DaoSupport.execute(sqlQuery, sessionContext, DATA_SOURCE_JNDI_NAME,
 *       RESOURCE_BUNDLE_NAME, divisionId, branchId, employeeId, periodTypeCode, periodNumber, periodYear,
 *       operatorId);
 *       
 *     String result = DaoSupport.execute(sqlQuery, sessionContext, DATA_SOURCE_JNDI_NAME,
 *       RESOURCE_BUNDLE_NAME, String.class, divisionId, branchId, employeeId, periodTypeCode, periodNumber, periodYear,
 *       operatorId);
 * 
 * 3. Пример использования метода find.
 * 
 *     List<ExportTaskDto> exportTaskList = DaoSupport.find(sqlQuery, sessionContext, DATA_SOURCE_JNDI_NAME,
 *       RESOURCE_BUNDLE_NAME, 
 *        new ResultSetMapper<ExportTaskDto>() {
 *        public void map(ResultSet rs, ExportTaskDto dto) throws SQLException {
 *          dto.setTaskId(getInteger(rs, "task_id")); // Обратите внимание на то, как достается значение типа Integer из ResultSet
 *          dto.setExportTaskTypeId(getInteger(rs, "export_task_type_id"));
 *          dto.setProcessName(rs.getString("process_name"));
 *          dto.setFormNameRus(rs.getString("form_name_rus"));
 *          dto.setTaskStatusCode(rs.getString("task_status_code"));
 *          dto.setTaskStatusName(rs.getString("task_status_name"));
 *          dto.setTaskCreateDate(getTimestamp(rs, "task_create_date")); // Обратите внимание на то, как достается значение типа Timestamp из ResultSet
 *          dto.setTaskOperatorId(getInteger(rs, "task_operator_id"));
 *          dto.setTaskOperatorName(rs.getString("task_operator_name"));
 *          dto.setTaskParamsStr(rs.getString("task_params_str"));
 *          dto.setTaskResultCode(rs.getString("task_result_code"));
 *          dto.setTaskResultName(rs.getString("task_result_name"));
 *        }
 *      }, ExportTaskDto.class, taskId, exportTaskTypeId, processName, beginDate, endDate, operatorId);
 *      
 *    Использование методов getInteger и getTimestamp необходимо для корректного получение данных из ResultSet (см. JavaDoc к данным методам).
 *
 * 4. Пример использования метода select.
 * 
 *     List<PositionTypeDto> positionTypeList = DaoSupport.<PositionTypeDto>select(sqlQuery, sessionContext,
 *      DATA_SOURCE_JNDI_NAME, RESOURCE_BUNDLE_NAME,
 *      new ResultSetMapper<PositionTypeDto>() {
 *        public void map(ResultSet rs, PositionTypeDto dto)
 *            throws SQLException {
 *          dto.setPositionTypeId(rs.getInt("position_type_id"));
 *          dto.setPositionTypeNameRus(rs.getString("position_type_name_rus"));
 *          dto.setPositionTypeNameEng(rs.getString("position_type_name_eng"));
 *        }
 *      }, PositionTypeDto.class);
 * 
 * 5. Пример использования метода update.
 * 
 *     DaoSupport.update(sqlQuery, sessionContext, DATA_SOURCE_JNDI_NAME, RESOURCE_BUNDLE_NAME, 
 *      taskId, operatorId);
 *        
 * 6. Пример использования метода delete.
 * 
 *     DaoSupport.delete(sqlQuery, sessionContext, DATA_SOURCE_JNDI_NAME, RESOURCE_BUNDLE_NAME, 
 *      taskId, operatorId);
 * </pre>
 */
public class DaoSupport {
	protected static Logger logger = Logger.getLogger(DaoSupport.class.getName());	
	
	/**
	 * Данный метод выполняет sql-выражение, создающее запись в БД.
	 * 
	 * @param <T> 								тип возвращаемого значения
	 * @param query 							текст запроса
	 * @param sessionContext      контекст
	 * @param dataSourceJndiName  jndi-имя источника данных
	 * @param resourceBundleName  идентификатор файла ресурсов
	 * @param params              параметры sql-выражения
	 * @param resultTypeClass     Тип возвращаемого значения
	 * @return идентификатор (первичный ключ) созданной записи
	 * @throws ApplicationException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T create(
			String query
			, SessionContext sessionContext
			, String dataSourceJndiName
			, String resourceBundleName
			, Class<T> resultTypeClass
			, Object... params)
			throws ApplicationException {
		
		logger.trace("create(..., " + dataSourceJndiName + ", ...)");
		
		T result = null;

		try {
			CallContext.begin(sessionContext, dataSourceJndiName, resourceBundleName);
			Db db = CallContext.getDb();
			
			CallableStatement callableStatement = db.prepare(query);
			
			setInputParamsToStatement(callableStatement, 2, params);

			if (resultTypeClass.equals(Integer.class)) {
				callableStatement.registerOutParameter(1, Types.INTEGER);
			} else if (resultTypeClass.equals(String.class)) {
				callableStatement.registerOutParameter(1, Types.VARCHAR);
			} else if (resultTypeClass.equals(Timestamp.class)) {
				callableStatement.registerOutParameter(1, Types.TIMESTAMP);
			} else if (resultTypeClass.equals(BigDecimal.class)) {
//				callableStatement.registerOutParameter(1, Types.DOUBLE);
				callableStatement.registerOutParameter(1, Types.NUMERIC);
			} else {
				throw new ApplicationException("Unknown result type", null);
			}
			
			// Выполнение запроса		
			callableStatement.execute();

			result = (T)callableStatement.getObject(1);
			if (callableStatement.wasNull())result = null;

		} catch (Throwable th) {
			sessionContext.setRollbackOnly();
			throw new ApplicationException(th.getMessage(), th);
		} finally {
			CallContext.end();
		}
		
		return result;
	}
	
	/**
	 * Данный метод выполняет sql-выражение без возвращаемого значения.
	 * 
	 * @param query 							текст запроса
	 * @param sessionContext      контекст
	 * @param dataSourceJndiName  jndi-имя источника данных
	 * @param resourceBundleName  идентификатор файла ресурсов
	 * @param params              параметры sql-выражения
	 * @throws ApplicationException
	 */
	public static void execute(
			String query
			, SessionContext sessionContext
			, String dataSourceJndiName
			, String resourceBundleName
			, Object... params)
			throws ApplicationException {
		
		logger.trace("execute1(..., " + dataSourceJndiName + ", ...)");

		try {
			CallContext.begin(sessionContext, dataSourceJndiName, resourceBundleName);
			Db db = CallContext.getDb();
			
			CallableStatement callableStatement = db.prepare(query);
			
			setInputParamsToStatement(callableStatement, 1, params);

			// Выполнение запроса		
			callableStatement.execute();

		} catch (Throwable th) {
			sessionContext.setRollbackOnly();
			throw new ApplicationException(th.getMessage(), th);
		} finally {
			CallContext.end();
		}
	}
	
	/**
	 * Данный метод выполняет sql-выражение, возвращающее курсор.
	 * 
	 * @param <T>					тип возвращаемого значения
	 * @param query					текст запроса
	 * @param sessionContext		контекст
	 * @param dataSourceJndiName	jndi-имя источника данных
	 * @param resourceBundleName	идентификатор файла ресурсов
	 * @param mapper				экземпляр класса, осуществляющего мэппинг полей dto и ResultSet
	 * @param recordClass				класс dto
	 * @param params				параметры sql-выражения
	 * @return						список объектов в виде List&lt;T&gt;
	 * @throws ApplicationException
	 */
	public static <T> List<T> find(
			String query
			, SessionContext sessionContext
			, String dataSourceJndiName
			, String resourceBundleName
			, ResultSetMapper<T> mapper
			, Class<T> recordClass
			, Object... params) 
			throws ApplicationException {
			
		return findOrSelect(query, sessionContext, dataSourceJndiName, resourceBundleName, mapper,
				recordClass, ExecutionType.CALLABLE_STATEMENT, params);
	}
	
	/**
	 * Данный метод выполняет sql-выражение, возвращающее объект типа T.
	 * 
	 * @param <T>					тип возвращаемого значения
	 * @param query					текст запроса
	 * @param sessionContext		контекст
	 * @param dataSourceJndiName	jndi-имя источника данных
	 * @param resourceBundleName	идентификатор файла ресурсов
	 * @param resultTypeClass		тип возвращаемого значения; для возврата нескольких значений используется массив типов - Object[].
	 *								Пример параметра, передаваемого при вызове: <code>new Object[] {Integer.class, String.class, Float.class}</code> 
	 * @param params				параметры sql-выражения
	 * @return объект типа T
	 * @throws ApplicationException
	 */
	public static <T> T execute(
			String query
			, SessionContext sessionContext
			, String dataSourceJndiName
			, String resourceBundleName
			, Class<T> resultTypeClass
			, Object... params) 
			throws ApplicationException {
		
		logger.trace("execute2(..., " + dataSourceJndiName + ", ...)");

		T result = null;

		try {
			CallContext.begin(sessionContext, dataSourceJndiName, resourceBundleName);
			Db db = CallContext.getDb();
			
			CallableStatement callableStatement = db.prepare(query);
			
			setInputParamsToStatement(
					callableStatement,
					resultTypeClass.isArray() ? 1 : 2,
					params);
			
			setOutputParamsToStatement(
					callableStatement,
					resultTypeClass,
					params);
			
			// Выполнение запроса		
			callableStatement.execute();

			result = getResult(callableStatement, resultTypeClass, params);

		} catch (Throwable th) {
			sessionContext.setRollbackOnly();
			throw new ApplicationException(th.getMessage(), th);
		} finally {
			CallContext.end();
		}
		
		return result;
	}

	private static <T> void setOutputParamsToStatement(
			CallableStatement callableStatement,
			Class<T> resultTypeClass,
			Object[] params) throws SQLException, ApplicationException {
		if(resultTypeClass.isArray()) {
			Object[] outputParamTypes = (Object[]) params[0];
			for(int i = 0; i < outputParamTypes.length; i++) {
				registerParameter(callableStatement, i + params.length, (Class<T>) outputParamTypes[i]);
			}
		} else {
			registerParameter(callableStatement, 1, resultTypeClass);
		}
	}

	private static <T> void registerParameter(
			CallableStatement callableStatement,
			int paramNumber,
			Class<T> resultTypeClass) throws SQLException, ApplicationException {
		if (resultTypeClass.equals(Integer.class)) {
			callableStatement.registerOutParameter(paramNumber, Types.INTEGER);
		} else if (resultTypeClass.equals(String.class)) {
			callableStatement.registerOutParameter(paramNumber, Types.VARCHAR);
		} else if (resultTypeClass.equals(Timestamp.class)) {
			callableStatement.registerOutParameter(paramNumber, Types.TIMESTAMP);
		} else if (resultTypeClass.equals(BigDecimal.class)) {
//			callableStatement.registerOutParameter(paramNumber, Types.DOUBLE);
			callableStatement.registerOutParameter(paramNumber, Types.NUMERIC);
		} else {
			throw new ApplicationException("Unknown result type", null);
		}
	}

	private static <T> T getResult(
			CallableStatement callableStatement,
			Class<T> resultTypeClass,
			Object[] params) throws SQLException {
		T result = null;
		
		if(resultTypeClass.isArray()) {
			Object[] outputParamTypes = (Object[]) params[0];
			Object[] results = new Object[outputParamTypes.length];
			for(int i = 0; i < outputParamTypes.length; i++) {
				results[i] = callableStatement.getObject(i + params.length);
				if (callableStatement.wasNull()) results[i] = null;
			}
			result = (T) results;
		} else {
			result = (T)callableStatement.getObject(1);
			if (callableStatement.wasNull())result = null;
		}
		
		return result;
	}
	
	/**
	 * Данный метод выполняет sql-запрос.
	 * 
	 * @param <T>					тип возвращаемого значения
	 * @param query					текст запроса
	 * @param sessionContext		контекст
	 * @param dataSourceJndiName	jndi-имя источника данных
	 * @param resourceBundleName	идентификатор файла ресурсов
	 * @param mapper				экземпляр класса, осуществляющего мэппинг полей dto и ResultSet
	 * @param modelClass            класс dto
	 * @param params				параметры sql-запрос
	 * @return						список объектов в виде List&lt;T&gt;
	 * @throws ApplicationException
	 */
	public static <T> List<T> select(
			String query
			, SessionContext sessionContext
			, String dataSourceJndiName
			, String resourceBundleName
			, ResultSetMapper<T> mapper
			, Class<T> modelClass
			, Object... params) 
			throws ApplicationException {
			
		return findOrSelect(query, sessionContext, dataSourceJndiName, resourceBundleName, mapper,
				modelClass, ExecutionType.QUERY, params);
	}
	
	/**
	 * Данный метод выполняет sql-выражение, изменяющее запись в БД.
	 * 
	 * @param query 							текст запроса
	 * @param sessionContext      контекст
	 * @param dataSourceJndiName  jndi-имя источника данных
	 * @param resourceBundleName  идентификатор файла ресурсов
	 * @param params              параметры sql-выражения
	 * @throws ApplicationException
	 */
	public static void update(
			String query
			, SessionContext sessionContext
			, String dataSourceJndiName
			, String resourceBundleName
			, Object... params)
			throws ApplicationException {
		
		logger.trace("BEGIN update(..., " + dataSourceJndiName + ", ...)");
			
		execute(query, sessionContext, dataSourceJndiName, resourceBundleName, params);
		
		logger.trace("END update(..., " + dataSourceJndiName + ", ...)");
	}
	
	/**
	 * Данный метод выполняет sql-выражение, удаляющее запись из БД.
	 * 
	 * @param query 							текст запроса
	 * @param sessionContext      контекст
	 * @param dataSourceJndiName  jndi-имя источника данных
	 * @param resourceBundleName  идентификатор файла ресурсов
	 * @param params              параметры sql-выражения
	 * @throws ApplicationException
	 */
	public static void delete(
			String query
			, SessionContext sessionContext
			, String dataSourceJndiName
			, String resourceBundleName
			, Object... params)
			throws ApplicationException {
		
		logger.trace("BEGIN delete(..., " + dataSourceJndiName + ", ...)");
		
		execute(query, sessionContext, dataSourceJndiName, resourceBundleName, params);

		logger.trace("END delete(..., " + dataSourceJndiName + ", ...)");
	}
	
	/**
	 * Вспомогательный метод, объединящий в себе логику работы методов find и select.
	 * 
	 * @param <T> 								тип возвращаемого значения
	 * @param query 							текст запроса
	 * @param sessionContext      контекст
	 * @param dataSourceJndiName  jndi-имя источника данных
	 * @param resourceBundleName  идентификатор файла ресурсов
	 * @param mapper              экземпляр класса, осуществляющего мэппинг полей dto и ResultSet
	 * @param recordClass класс записи
	 * @param params параметры sql-запроса или sql-выражения
	 * @return список объектов в виде List&lt;T&gt;
	 * @throws ApplicationException
	 */
	private static <T> List<T> findOrSelect(
			String query
			, SessionContext sessionContext
			, String dataSourceJndiName
			, String resourceBundleName
			, ResultSetMapper<T> mapper
			, Class<T> recordClass
			, ExecutionType executionType
			, Object... params)
			throws ApplicationException {
		
		logger.trace("BEGIN findOrSelect(..., " + dataSourceJndiName + ", ...)");
			
		List<T> result = new ArrayList<T>();
	
		try {
			CallContext.begin(sessionContext, dataSourceJndiName, resourceBundleName);
			Db db = CallContext.getDb();
			
			CallableStatement callableStatement = db.prepare(query);
		
			ResultSet resultSet = setParamsAndExecute(callableStatement, executionType, params);
			
			while (resultSet.next()) {
				T resultModel = recordClass.newInstance();
				
				mapper.map(resultSet, resultModel);
				
				result.add(resultModel);
			}
		} catch (Throwable th) {
			sessionContext.setRollbackOnly();
			throw new ApplicationException(th.getMessage(), th);
		} finally {
			CallContext.end();
			logger.trace("END findOrSelect(..., " + dataSourceJndiName + ", ...)");
		}
		
		return result;
	}
	
	/**
	 * Вспомогательный метод, выставляющий параметры callableStatement и выполняющий запрос.
	 * 
	 * @param callableStatement    экземпляр callableStatement
	 * @param executionType        данная переменная используется для определения того,
	 * 														 работаем ли мы с sql-выражением, или sql-запросом
	 * @param params               параметры sql-запроса или sql-выражения
	 * @return экземпляр ResultSet
	 * @throws SQLException
	 */
	private static ResultSet setParamsAndExecute(
			CallableStatement callableStatement, ExecutionType executionType, Object... params)
			throws SQLException {
		
		logger.trace("setParamsAndExecute(...)");
		
		ResultSet resultSet = null;
		if (executionType == ExecutionType.CALLABLE_STATEMENT) {
			callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
	
			setInputParamsToStatement(callableStatement, 2, params);
			
			// Выполнение запроса.
			callableStatement.execute();
	
			//Получим набор.
			resultSet = (ResultSet) callableStatement.getObject(1);
		} else if (executionType == ExecutionType.QUERY) {
			setInputParamsToStatement(callableStatement, 1, params);
			
			//Получим набор.
			resultSet = callableStatement.executeQuery();
		}
		return resultSet;
	}

	/**
	 * Данный вспомогательный метод присваивает параметры запроса объекту callableStatement.
	 * 
	 * @param callableStatement 	экзепляр callableStatement
	 * @param i                 	номер с, с которого начинаем выставлять параметры
	 * @param params            	параметры запроса
	 * @throws SQLException
	 */
	public static void setInputParamsToStatement(
			CallableStatement callableStatement,
			int i,
			Object... params) throws SQLException {
		if (params.length > 0) {
			for (int index = ((params[0] != null && params[0].getClass().isArray()) ? 1 : 0); index < params.length; index++) {
				Object param = params[index];
	
				if (param != null) {
					Class<?> clazz = param.getClass();
					if (clazz.equals(Integer.class)) {
						setIntegerParameter(callableStatement, (Integer)param, i);
					} else if (clazz.equals(String.class)) {
						setStringParameter(callableStatement, (String)param, i);
					} else if (clazz.equals(Boolean.class)) {
						setBooleanParameter(callableStatement, (Boolean)param, i);
					} else if (clazz.equals(BigDecimal.class)) {
						setBigDecimalParameter(callableStatement, (BigDecimal)param, i);
					} else if (clazz.equals(java.util.Date.class)) {
						setDateParameter(callableStatement, (java.util.Date)param, i);
					} else {
						callableStatement.setObject(i, param);
					}
				} else {
					callableStatement.setNull(i, Types.NULL);
				}
				i++;
			}
		}
	}

	/**
	 * Вспомогательный метод. Используется для задания строкового параметра объекту callableStatement.
	 * 
	 * @param callableStatement    экзепляр callableStatement
	 * @param parameter            параметр
	 * @param place                место вставки параметра
	 * @throws SQLException
	 */
	private static void setStringParameter(
		CallableStatement callableStatement
		, String parameter
		, int place) 
		throws SQLException {

		if(JepRiaUtil.isEmpty(parameter)) {
			callableStatement.setNull(place, Types.VARCHAR);
		} else {
			callableStatement.setString(place, parameter);
		}
	}
	
	/**
	 * Вспомогательный метод. Используется для задания целочисленного параметра объекту callableStatement.
	 * 
	 * @param callableStatement    экзепляр callableStatement
	 * @param parameter            параметр
	 * @param place                место вставки параметра
	 * @throws SQLException
	 */
	private static void setIntegerParameter(
		CallableStatement callableStatement
		, Integer parameter
		, int place) 
		throws SQLException {

		if(JepRiaUtil.isEmpty(parameter)) {
			callableStatement.setNull(place, Types.INTEGER);
		} else {
			callableStatement.setInt(place, parameter);
		}
	}
	
	/**
	 * Вспомогательный метод. Используется для задания булевого параметра объекту callableStatement.
	 * 
	 * @param callableStatement    экзепляр callableStatement
	 * @param parameter            параметр
	 * @param place                место вставки параметра
	 * @throws SQLException
	 */
	private static void setBooleanParameter(
		CallableStatement callableStatement
		, Boolean parameter
		, int place) 
		throws SQLException {

		if(parameter) {
			callableStatement.setInt(place, 1);
		} else {
			callableStatement.setInt(place, 0);
		}
	}
	
	/**
	 * Вспомогательный метод. Используется для задания параметра типа BigDecimal объекту callableStatement. 
	 * 
	 * @param callableStatement    экзепляр callableStatement
	 * @param parameter            параметр
	 * @param place                место вставки параметра
	 * @throws SQLException
	 */
	private static void setBigDecimalParameter(
		CallableStatement callableStatement
		, BigDecimal parameter
		, int place) 
		throws SQLException {

		if(parameter == null) {
			callableStatement.setNull(place, Types.NUMERIC);
		} else {
			callableStatement.setBigDecimal(place, parameter);
		}
	}

	/**
	 * Вспомогательный метод. Используется для задания параметра типа Date объекту callableStatement. 
	 * 
	 * @param callableStatement    экземпляр callableStatement
	 * @param parameter            параметр
	 * @param place                место вставки параметра
	 * @throws SQLException
	 */
	private static void setDateParameter(CallableStatement callableStatement, java.util.Date parameter, int place)
			throws SQLException {
		if (parameter == null) {
			callableStatement.setNull(place, Types.TIMESTAMP);
		} else {
			callableStatement.setTimestamp(place, new java.sql.Timestamp(parameter.getTime()));
		}
	}
	
	/**
	 * Данный перечислимый тип содержит тип, определяющий работаем мы с
	 * sql-выражением или sql-запросом. Используется в методе setParamsAndExecute.
	 */
	private enum ExecutionType {
		QUERY, CALLABLE_STATEMENT
	}
}