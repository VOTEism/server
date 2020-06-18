/*
 * Copyright 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */
package com.voteism.domain;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.stereotype.Component;

/**
 * Token manager class that sets the user access token (JWT) and user phone number retrieved from the token to identify the user
 * 
 * @author Ranjit Kollu
 *
 */
@Component
public class TokenQueueManager {
	private String jwtToken;
	private static BlockingQueue<String> userTokenQueue = new LinkedBlockingQueue<String>();
	private static BlockingQueue<String> userRequestQueue = new LinkedBlockingQueue<String>();

	/**
	 * Set the token
	 * 
	 * @param token
	 */
	public void setJwtToken(String token) {
		this.jwtToken = token;
	}

	/**
	 * Get the token
	 * 
	 * @return jwtToken
	 */
	public String getJwtToken() {
		return this.jwtToken;
	}

	/**
	 * Get the user token queue containing the user tokens
	 *  
	 * @return userTokenQueue
	 */
	public static BlockingQueue<String> getUserTokenQueue() {
		return userTokenQueue;
	}

	/**
	 * Insert the user token into the queue
	 * 
	 * @param token
	 * @throws InterruptedException
	 */
	public static void insertToken(String token) throws InterruptedException {
		userTokenQueue.put(token);
	}

	/**
	 * Get the user token queue containing the user phone number
	 *  
	 * @return userRequestQueue
	 */
	public static BlockingQueue<String> getUserRequestQueue() {
		return userRequestQueue;
	}

	/**
	 * Insert the user phone number from the token
	 * 
	 * @param phonenumber
	 * @throws InterruptedException
	 */
	public static void insertRequestUser(String phonenumber) throws InterruptedException {
		userRequestQueue.put(phonenumber);
	}
}
