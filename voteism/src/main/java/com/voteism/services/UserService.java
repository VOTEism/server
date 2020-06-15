/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.services;

import java.util.Map;

import com.voteism.domain.User;
import com.voteism.exceptions.LoginException;
import com.voteism.exceptions.UserException;

/**
 * User operations interface
 * 
 * @author Ranjit Kollu
 *
 */
public interface UserService {
	/**
	 * Check if the user exists with the phone number
	 * 
	 * @param phonenumber user phone number
	 * @return True if the user exists, otherwise return false
	 * @throws UserException
	 */
	public boolean doesUserExist(final String phonenumber) throws UserException;
	
	/**
	 * Add the user to the app
	 * 
	 * @param user User to add
	 * @return User added
	 * @throws UserException
	 */
	public User addUser(final User user) throws UserException;
	
	/**
	 * Try to register the user and send the user a OTP code to complete the registration
	 * 
	 * @param user User to register
	 * @return Map containing the OTP status code (NOT_VERIFIED)
	 * @throws LoginException
	 */
	public Map<String, Object> register(final User user) throws LoginException;
	
	/**
	 * Login the user
	 * 
	 * @param user User logging in
	 * @return Map containing the user that logged in, if the user changes device to login we re-send the OTP to the new device to login, and NOT_VERIFIED in
	 * response map.
	 * @throws UserException
	 * @throws LoginException
	 */
	public Map<String, Object> login(final User user) throws UserException, LoginException;
	
	/**
	 * Logout the user
	 * 
	 * @param phonenumber User to logout
	 * @throws UserException
	 */
	public void logout(final String phonenumber) throws UserException;
}
