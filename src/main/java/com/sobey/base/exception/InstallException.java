package com.sobey.base.exception;

public class InstallException extends RuntimeException {

	public InstallException() {
		super();
	}

	public InstallException(final String message) {
		super(message);
	}

	public InstallException(final String message, final Throwable t) {
		super(message, t);
	}

	public InstallException(final Throwable t) {
		super(t);
	}

}
