package com.tweetapp.exception;

public class EmailAlreadyExistsException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EmailAlreadyExistsException(String str) {
		super(str);
	}
}
