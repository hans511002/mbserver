package com.sobey.base.exception;

public class NoMatchException extends RuntimeException {
	public NoMatchException() {
		super();
	}

	public NoMatchException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;

	public NoMatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoMatchException(Throwable cause) {
		super(cause);
	}

}
