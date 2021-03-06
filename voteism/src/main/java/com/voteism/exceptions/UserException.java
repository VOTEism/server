/*
 * Copyright 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.exceptions;

/**
 * User exception class
 * 
 * @author Ranjit Kollu
 *
 */
public class UserException extends Exception {
	/**
	 * Constructor
	 */
	public UserException() {
		super();
	}
	
	/**
	 * Constructor 
	 * 
	 * @param message Error message
	 * @param cause Cause of the exception
	 */
	public UserException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructor
	 * 
	 * @param message Error message
	 */
	public UserException(final String message) {
		super(message);
	}
	
	/**
	 * Constructor
	 * 
	 * @param cause Cause of the exception
	 */
	public UserException(final Throwable cause) {
		super(cause);
	}
}