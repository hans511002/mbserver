package com.sobey.base.exception;

import java.net.SocketException;

public class SocketMessageException extends SocketException {
	public SocketMessageException() {
		super();
	}

	private static final long serialVersionUID = 1L;

	public SocketMessageException(String message) {
		super(message);
	}

}
