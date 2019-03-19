package com.sobey.base.exception;

import java.sql.SQLException;

public class NotInitDaoPOException extends SQLException {
	public NotInitDaoPOException() {
		super();
	}

	public NotInitDaoPOException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;

	public NotInitDaoPOException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotInitDaoPOException(Throwable cause) {
		super(cause);
	}

}
