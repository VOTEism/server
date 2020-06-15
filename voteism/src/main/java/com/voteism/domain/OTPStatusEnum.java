/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */
package com.voteism.domain;

/**
 * Enum for OTP status'es
 * 
 * @author Ranjit Kollu
 *
 */
public enum OTPStatusEnum {
	NOT_VERIFIED("Not Verified"),
	OK("ok"),
	FAIL("Fail"),
	EXPIRED("Expired"),
	RESEND_NOT_ALLOWED("Resend not allowed");
	
	private final String status;
	
	private OTPStatusEnum(String status) {
		this.status = status;
	}
	
	public String getOtpStatus() {
		return this.status;
	}
}
