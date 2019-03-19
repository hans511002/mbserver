package com.sobey.base.exception;

public class DbException extends RuntimeException {

	public DbException() {
		super();
	}

	public DbException(final String message) {
		super(message);
	}

	public DbException(final String message, final Throwable t) {
		super(message, t);
	}

	public DbException(final Throwable t) {
		super(t);
	}

}
