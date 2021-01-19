package edu.intech.meetings.exceptions;

public class DaoException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public DaoException(final String message) {
		super(message);
	}

	public DaoException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
