package com.technology.jep.jepria.server.upload.blob;

import com.technology.jep.jepria.server.db.blob.BinaryLargeObject;
import com.technology.jep.jepria.server.dao.CallContext;
import com.technology.jep.jepria.server.exceptions.SpaceException;
import com.technology.jep.jepria.server.upload.AbstractFileUpload;
import com.technology.jep.jepria.shared.exceptions.ApplicationException;

/**
 * FileUpload Stateful Session EJB 3 для записи в BINARY_FILE
 */
public class BinaryFileUploadImpl extends AbstractFileUpload implements BinaryFileUpload {

	public int beginWrite(
		String tableName
		, String fileFieldName
		, String keyFieldName
		, Object rowId
		, String dataSourceJndiName) 
		throws ApplicationException {

		int result = -1;
		try {
			CallContext.begin(dataSourceJndiName);

			super.largeObject = new BinaryLargeObject(tableName, fileFieldName, keyFieldName, rowId);
			result = ((BinaryLargeObject)super.largeObject).beginWrite();
		} catch (ApplicationException ex) {
			cancel();
			throw ex;
		} finally {
			storedContext = CallContext.detach();
		}

		return result;
	}
	
	public void continueWrite(byte[] dataBlock) throws SpaceException {
		CallContext.attach(storedContext);
		boolean cancelled = false;
		try {
			((BinaryLargeObject)super.largeObject).continueWrite(dataBlock);
		} catch (Throwable ex) {
			cancelled = true;
			if (ex instanceof SpaceException) {
				throw (SpaceException) ex;
			} else if (ex instanceof Exception) {
				throw new SpaceException("continue write error", (Exception) ex);
			} else {
				throw new SpaceException("continue write error", new RuntimeException(ex));
			}
		} finally {
			if (cancelled) {
				try {
					cancel();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			storedContext = CallContext.detach();
		}
	}
}
