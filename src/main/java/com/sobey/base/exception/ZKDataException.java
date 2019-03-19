package com.sobey.base.exception;

public class ZKDataException extends RuntimeException {

	private static final long serialVersionUID = -8609624985184291552L;

	public ZKDataException() {
		super();
	}

	public ZKDataException(final String message) {
		super(message);
	}

	public ZKDataException(final String message, final Throwable t) {
		super(message, t);
	}

	public ZKDataException(final Throwable t) {
		super(t);
	}

}
