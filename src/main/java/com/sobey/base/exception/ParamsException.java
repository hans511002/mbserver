package com.sobey.base.exception;


public class ParamsException extends RuntimeException {

	public ParamsException() {
		super();
	}

	public ParamsException(final String message) {
		super(message);
	}

	public ParamsException(final String message, final Throwable t) {
		super(message, t);
	}

	public ParamsException(final Throwable t) {
		super(t);
	}

}
