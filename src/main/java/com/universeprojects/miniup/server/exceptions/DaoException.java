package com.universeprojects.miniup.server.exceptions;

public class DaoException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DaoException(String message, Throwable cause) {
		super(message, cause);
	}

	public DaoException(String message) {
		super(message);
	}
}
