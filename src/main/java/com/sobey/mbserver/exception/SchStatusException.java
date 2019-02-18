package com.sobey.mbserver.exception;

public class SchStatusException extends RuntimeException {

	public SchStatusException() {
		super();
	}

	public SchStatusException(final String message) {
		super(message);
	}

	public SchStatusException(final String message, final Throwable t) {
		super(message, t);
	}

	public SchStatusException(final Throwable t) {
		super(t);
	}

}
