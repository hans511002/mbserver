package com.sobey.base.exception;

public class POException extends Exception {
	public POException() {
		super();
	}

	public POException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;

	public POException(String message, Throwable cause) {
		super(message, cause);
	}

	public POException(Throwable cause) {
		super(cause);
	}

}
