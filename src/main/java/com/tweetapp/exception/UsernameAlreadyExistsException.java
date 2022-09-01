package com.tweetapp.exception;

public class UsernameAlreadyExistsException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UsernameAlreadyExistsException(String str) {
		super(str);
	}
}
