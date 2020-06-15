/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.exceptions;

/**
 * Phone exception
 * 
 * @author Ranjit Kollu
 *
 */
public class PhoneException extends Exception {
	/**
	 * Constructor
	 */
	public PhoneException() {
		super();
	}
	
	/**
	 * Constructor 
	 * 
	 * @param message Error message
	 * @param cause Cause of the exception
	 */
	public PhoneException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructor
	 * 
	 * @param message Error message
	 */
	public PhoneException(final String message) {
		super(message);
	}
	
	/**
	 * Constructor
	 * 
	 * @param cause Cause of the exception
	 */
	public PhoneException(final Throwable cause) {
		super(cause);
	}
}

