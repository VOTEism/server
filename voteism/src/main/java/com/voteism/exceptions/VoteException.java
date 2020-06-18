/*
 * Copyright 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.exceptions;

/**
 * Vote Exception class
 * 
 * @author Ranjit Kollu
 *
 */
public class VoteException extends Exception {
	/**
	 * Constructor
	 */
	public VoteException() {
		super();
	}
	
	/**
	 * Constructor 
	 * 
	 * @param message Error message
	 * @param cause Cause of the exception
	 */
	public VoteException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructor
	 * 
	 * @param message Error message
	 */
	public VoteException(final String message) {
		super(message);
	}
	
	/**
	 * Constructor
	 * 
	 * @param cause Cause of the exception
	 */
	public VoteException(final Throwable cause) {
		super(cause);
	}
}