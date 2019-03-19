package com.sobey.base.exception;

import java.io.IOException;

public class RemoteAccessException extends IOException {
	public RemoteAccessException() {
		super();
	}

	public RemoteAccessException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;

	public RemoteAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public RemoteAccessException(Throwable cause) {
		super(cause);
	}

}
