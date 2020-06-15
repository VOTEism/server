/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */
package com.voteism.domain;

import java.util.Date;

/**
 * OTP class containing the otp information
 * 
 * @author Ranjit Kollu
 *
 */
public class Otp {	
	private String phoneNumber;
	private long otp;
	private Date generatedTime;
	private String otpStatus;
	
	/**
	 * Constructor
	 */
	public Otp() {}
	
	/**
	 * Constructor
	 * 
	 * @param phoneNumber phone number
	 * @param otp otp
	 * @param generatedTime otp generated time
	 * @param otpStatus status of OTP
	 */
	public Otp(String phoneNumber, long otp, Date generatedTime, String otpStatus) {
		this.phoneNumber = phoneNumber;
		this.otp = otp;
		this.generatedTime = generatedTime;
		this.otpStatus = otpStatus;
	}
	
	/**
	 * Get phone number
	 * 
	 * @return phone number
	 */
    public String getPhoneNumber() {
    	return this.phoneNumber;
    }
    
    /**
     * Set the phone number
     * 
     * @param phoneNumber phone number
     */
    public void setPhoneNumber(String phoneNumber) {
    	this.phoneNumber = phoneNumber;
    }
    
	/**
	 * Get OTP
	 * 
	 * @return OTP
	 */
    public long getOTP() {
    	return this.otp;
    }
    
    /**
     * Set the OTP
     * 
     * @param otp otp
     */
    public void setOTP(long otp) {
    	this.otp = otp;
    }
    
	/**
	 * Get OTP generated time
	 * 
	 * @return OTP generated time
	 */
    public Date getGeneratedTime() {
    	return this.generatedTime;
    }
    
    /**
     * Set the OTP generated time
     * 
     * @param generatedTime otp generated time
     */
    public void setGeneratedTime(Date generatedTime) {
    	this.generatedTime = generatedTime;
    }
    
	/**
	 * Get OTP status
	 * 
	 * @return otp status
	 */

    public String getOtpStatus() {
    	return this.otpStatus;
    }
    
    /**
     * Set the otp status
     * 
     * @param otpStatus otp status
     */
    public void setOtpStatus(String otpStatus) {
    	this.otpStatus = otpStatus;
    }
}
