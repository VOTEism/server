/*
 * Copyright 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.services;

import java.util.Map;

import com.google.i18n.phonenumbers.NumberParseException;
import com.voteism.exceptions.PhoneException;

/**
 * Phone operations interface
 * 
 * @author Ranjit Kollu
 *
 */
public interface PhoneService {
	/**
	 * Check if the phone number is mobile number or not
	 * 
	 * @param phonenumber
	 * @return True if the phone number is a mobile number otherwise return false
	 * @throws NumberParseException
	 */
	public boolean isMobile(final String phonenumber) throws NumberParseException;
	
	/**
	 * Generate OTP and send the SMS to the user
	 * 
	 * @param phonenumber Phone number to send the SMS to
	 * @return Map containing the status of SMS (for a successful SMS send, the user gets NOT_VERIFIED status in the map)
	 * @throws PhoneException
	 */
	public Map<String, Object> generateOTPAndSendSMS(final String phonenumber) throws PhoneException;
	
	/**
	 * Resend OTP as SMS to the user
	 * 
	 * @param phonenumber Phone number to resend the SMS to
	 * @return Map containing the status of SMS (for a successful SMS resend, the user gets NOT_VERIFIED status in the map)
	 * @throws PhoneException
	 */
	public Map<String, Object> resendOTP(final String phonenumber) throws PhoneException;
	
	/**
	 * Verify the OTP entered by the user on the client application
	 * 
	 * @param phonenumber Phone number to which the OTP SMS was sent
	 * @param otp OTP sent
	 * @return Containing the verification status, verification status'es are - FAIL, EXPIRED, OK, NOT_VERIFIED
	 * @throws PhoneException
	 */
	public Map<String, Object> verifyOTP(final String phonenumber, final long otp) throws PhoneException;
}
