package com.tweetapp.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import com.tweetapp.constants.Constants;
import com.tweetapp.exception.EmailAlreadyExistsException;
import com.tweetapp.exception.UsernameAlreadyExistsException;
import com.tweetapp.model.Response;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<Response> handleEmailAlreadyExistsException(EmailAlreadyExistsException exception) {
		return new ResponseEntity<Response>(new Response(Constants.FAILED, Constants.BAD_REQUEST, "Email Already Exists !"), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(UsernameAlreadyExistsException.class)
	public ResponseEntity<Response> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException exception) {
		return new ResponseEntity<Response>(new Response(Constants.FAILED, Constants.BAD_REQUEST, "Username Already Exists !"), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Response> handleResponseStatusException(ResponseStatusException exception) {
		return new ResponseEntity<Response>(new Response(Constants.FAILED, Constants.FORBIDDEN, "Unable to login, Please check credentials"), HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Response> handleException(Exception exception) {
		return new ResponseEntity<Response>(new Response(Constants.FAILED, Constants.INTERNAL_ERROR, exception.getLocalizedMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
}
