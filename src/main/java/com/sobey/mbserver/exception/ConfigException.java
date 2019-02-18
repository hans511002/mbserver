package com.sobey.mbserver.exception;

import java.io.IOException;

public class ConfigException extends IOException {

	public ConfigException() {
		super();
	}

	public ConfigException(final String message) {
		super(message);
	}

	public ConfigException(final String message, final Throwable t) {
		super(message, t);
	}

	public ConfigException(final Throwable t) {
		super(t);
	}

}
