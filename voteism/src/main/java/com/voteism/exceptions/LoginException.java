/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.exceptions;

/**
 * Login exception class
 * 
 * @author Ranjit Kollu
 *
 */
public class LoginException extends Exception {
	/**
	 * Constructor
	 */
	public LoginException() {
		super();
	}
	
	/**
	 * Constructor 
	 * 
	 * @param message Error message
	 * @param cause Cause of the exception
	 */
	public LoginException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructor
	 * 
	 * @param message Error message
	 */
	public LoginException(final String message) {
		super(message);
	}
	
	/**
	 * Constructor
	 * 
	 * @param cause Cause of the exception
	 */
	public LoginException(final Throwable cause) {
		super(cause);
	}
}
