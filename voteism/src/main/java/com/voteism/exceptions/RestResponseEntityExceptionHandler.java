/*
 * Copyright 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * Generic exception handler
 * 
 * @author Ranjit Kollu
 *
 */
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	public RestResponseEntityExceptionHandler() {
		super();
	}

	@ExceptionHandler({ LoginException.class })
	public ResponseEntity<Object> handleAll(LoginException ex, WebRequest request) {
		return handleExceptionInternal(ex, ex.getLocalizedMessage(), new HttpHeaders(),
				HttpStatus.FORBIDDEN, request); 
	}
	
	@ExceptionHandler({ Exception.class })
	public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
		if(ex.getCause().getClass().getName().equalsIgnoreCase("com.voteism.exceptions.LoginException")) {
			return handleExceptionInternal((Exception) ex.getCause(), ex.getCause().getLocalizedMessage(), new HttpHeaders(),
					HttpStatus.FORBIDDEN, request); 
		}
		
		return handleExceptionInternal(ex, ex.getLocalizedMessage(), new HttpHeaders(),
				HttpStatus.INTERNAL_SERVER_ERROR, request);
	}

	@ExceptionHandler({ RuntimeException.class })
	public ResponseEntity<Object> handleAll(RuntimeException ex, WebRequest request) {
		if(ex.getCause().getClass().getName().equalsIgnoreCase("com.voteism.exceptions.LoginException")) {
			return handleExceptionInternal(ex, ex.getCause().getLocalizedMessage(), new HttpHeaders(),
					HttpStatus.FORBIDDEN, request); 
		}
		
		return handleExceptionInternal(ex, ex.getLocalizedMessage(), new HttpHeaders(),
				HttpStatus.INTERNAL_SERVER_ERROR, request);
	}
	
	@ExceptionHandler({ Throwable.class })
	public ResponseEntity<Object> handleAll(Throwable ex, WebRequest request) {
		if(ex.getClass().getName().equalsIgnoreCase("com.voteism.exceptions.LoginException")) {
			return handleExceptionInternal((Exception) ex.getCause(), ex.getCause().getLocalizedMessage(), new HttpHeaders(),
					HttpStatus.FORBIDDEN, request); 
		}
		
		return handleExceptionInternal((Exception) ex.getCause(), ex.getLocalizedMessage(), new HttpHeaders(),
				HttpStatus.INTERNAL_SERVER_ERROR, request);
	}
}
