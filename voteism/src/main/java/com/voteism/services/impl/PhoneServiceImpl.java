/*
 * Copyright 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.services.impl;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.voteism.ses.SendWithSes;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.NumberParseException.ErrorType;
import com.voteism.domain.OTPStatusEnum;
import com.voteism.domain.Otp;
import com.voteism.exceptions.PhoneException;
import com.voteism.firestore.FirestoreInitializer;
import com.voteism.services.PhoneService;
import com.voteism.utils.VoteismUtils;

/**
 * Implementation class for Phone operations
 * 
 * @author Ranjit Kollu
 *
 */
@Component
public class PhoneServiceImpl implements PhoneService {
	private static final Logger logger = LoggerFactory.getLogger(PhoneServiceImpl.class);
	
	public boolean isMobile(final String phonenumber) throws NumberParseException {
		logger.info("Verifying if the phone number is mobile or not");
		
		if (!VoteismUtils.isValidPhoneNumber(phonenumber, true)) {
			throw new NumberParseException(ErrorType.NOT_A_NUMBER,
					"This is not a valid mobile phone number, please enter a valid mobile phone number!");
		}

		return VoteismUtils.isMobile(phonenumber);
	}

	public Map<String, Object> generateOTPAndSendSMS(final String phonenumber) throws PhoneException {
		logger.info("Generating the OTP and sending the SMS for the phonenumber");
		
		try {
			final Map<String, Object> sendOTPMap = new HashMap<String, Object>();
			final long otp = VoteismUtils.getOTPwithMinLength(Instant.now());
			
			addOTP(phonenumber, otp);
			SendWithSes.sendSMSWithSes(phonenumber, otp);
			sendOTPMap.put("otpstatus", OTPStatusEnum.NOT_VERIFIED.name());
			
			logger.info("Successfully generated the OTP for the phonenumber");
			return sendOTPMap;
		} catch (InvalidKeyException | NoSuchAlgorithmException | ExecutionException | InterruptedException
				| ParseException ex) {
			throw new PhoneException(ex.getLocalizedMessage());
		}
	}

	public Map<String, Object> verifyOTP(final String phonenumber, final long otp) throws PhoneException {
		logger.info("Verifying the OTP for the phonenumber");
		
		try {
			final Map<String, Object> verifyOTPMap = new HashMap<String, Object>();
			final Otp otpObj = getOtpForPhoneNumber(phonenumber);
			String retStatus = otpObj.getOtpStatus();

			if (otpObj.getOtpStatus().equalsIgnoreCase(OTPStatusEnum.NOT_VERIFIED.name())
					|| otpObj.getOtpStatus().equalsIgnoreCase(OTPStatusEnum.FAIL.name())) {
				if (otpObj.getOTP() == otp) {
					if (VoteismUtils.OTPExpired(otpObj.getGeneratedTime())) {
						updateOTPStatus(phonenumber, OTPStatusEnum.EXPIRED.name());
						retStatus = OTPStatusEnum.EXPIRED.name();
					} else {
						updateOTPStatus(phonenumber, OTPStatusEnum.OK.name());
						retStatus = OTPStatusEnum.OK.name();
					}
				} else {
					updateOTPStatus(phonenumber, OTPStatusEnum.FAIL.name());
					retStatus = OTPStatusEnum.FAIL.name();
				}
			}

			verifyOTPMap.put("otpstatus", retStatus);
			return verifyOTPMap;
		} catch (InterruptedException | ExecutionException | ParseException ex) {
			throw new PhoneException(ex.getLocalizedMessage());
		}
	}

	public Map<String, Object> resendOTP(final String phonenumber) throws PhoneException {
		logger.info("Generating the OTP and re-sending the SMS for the phonenumber");
		try {
			final Map<String, Object> sendOTPMap = new HashMap<String, Object>();
			final long otp = VoteismUtils.getOTPwithMinLength(Instant.now());
	
			updateOTP(phonenumber, otp, OTPStatusEnum.NOT_VERIFIED.name());
			SendWithSes.sendSMSWithSes(phonenumber, otp);
			sendOTPMap.put("otpstatus", OTPStatusEnum.NOT_VERIFIED.name());
	
			logger.info("Successfully re-generated the OTP for the phonenumber");
			return sendOTPMap;
		} catch(InvalidKeyException | NoSuchAlgorithmException | ParseException | InterruptedException | ExecutionException ex) {
			throw new PhoneException(ex.getLocalizedMessage());
		}
	}

	private Otp getOtpForPhoneNumber(final String phonenumber) throws ExecutionException, InterruptedException {
		final Firestore firestore = FirestoreInitializer.getFirestoreInstance();
		final DocumentReference docRef = firestore.collection("otp").document(phonenumber);
		final ApiFuture<DocumentSnapshot> future = docRef.get();
		final DocumentSnapshot document = future.get();

		if (document.exists()) {
			return new Otp(phonenumber, document.getLong("otp"), document.getTimestamp("generatedTime").toDate(),
					document.getString("status"));
		}

		return new Otp();
	}

	private boolean recordForPhonenumberExists(final String phonenumber) throws ExecutionException, InterruptedException {
		logger.info("Check if the phone number exists in the db!");
		
		final Firestore firestore = FirestoreInitializer.getFirestoreInstance();
		final DocumentReference docRef = firestore.collection("otp").document(phonenumber);
		final ApiFuture<DocumentSnapshot> future = docRef.get();
		final DocumentSnapshot document = future.get();
		
		return document.exists();
	}

	private void addOTP(final String phonenumber, final long otp) throws ExecutionException, InterruptedException, ParseException {
		logger.info("Add the OTP object to the db");
		
		final Firestore firestore = FirestoreInitializer.getFirestoreInstance();

		final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yy, HH:mm:ss.SSS a");
		final LocalDateTime now = LocalDateTime.now();
		final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy, HH:mm:ss.SSS a", Locale.ENGLISH);
		formatter.setTimeZone(TimeZone.getTimeZone(VoteismUtils.getTimezoneString()));
		
		final Date date = formatter.parse(dtf.format(now));
		final Map<String, Object> docData = new HashMap<>();

		docData.put("phonenumber", phonenumber);
		docData.put("otp", otp);
		docData.put("generatedTime", date);
		docData.put("status", OTPStatusEnum.NOT_VERIFIED.name());

		final ApiFuture<WriteResult> future = firestore.collection("otp").document(phonenumber).set(docData);
		logger.info("OTP written time : " + future.get().getUpdateTime());
		logger.info("Successfully added the OTP object to the db");
	}

	private void updateOTP(final String phonenumber, final long otp, final String otpStatus)
			throws ExecutionException, InterruptedException, ParseException {
		logger.info("Update the  OTP object in the db");
		
		if (recordForPhonenumberExists(phonenumber)) {
			final Firestore firestore = FirestoreInitializer.getFirestoreInstance();

			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yy, HH:mm:ss.SSS a");
			final LocalDateTime now = LocalDateTime.now();
			final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy, HH:mm:ss.SSS a", Locale.ENGLISH);
			formatter.setTimeZone(TimeZone.getTimeZone(VoteismUtils.getTimezoneString()));
			final Date date = formatter.parse(dtf.format(now));

			final Map<String, Object> docData = new HashMap<>();

			docData.put("otp", otp);
			docData.put("generatedTime", date);
			docData.put("status", otpStatus);

			final ApiFuture<WriteResult> future = firestore.collection("otp").document(phonenumber).set(docData);
			logger.info("OTP updated time : " + future.get().getUpdateTime());
			logger.info("Successfully updated the OTP object in the db");
		}
	}

	private void updateOTPStatus(final String phonenumber, final String otpStatus)
			throws ExecutionException, InterruptedException, ParseException {
		logger.info("Update the OTP status for the phonenumber in the db");
		final Otp otpObj = getOtpForPhoneNumber(phonenumber);

		if (otpObj.getPhoneNumber().equalsIgnoreCase(phonenumber)) {
			final Firestore firestore = FirestoreInitializer.getFirestoreInstance();

			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yy, HH:mm:ss.SSS a");
			final LocalDateTime now = LocalDateTime.now();
			final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy, HH:mm:ss.SSS a", Locale.ENGLISH);
			formatter.setTimeZone(TimeZone.getTimeZone(VoteismUtils.getTimezoneString()));
			final Date date = formatter.parse(dtf.format(now));

			final Map<String, Object> docData = new HashMap<>();

			docData.put("otp", otpObj.getOTP());
			docData.put("generatedTime", date);
			docData.put("status", otpStatus);

			final ApiFuture<WriteResult> future = firestore.collection("otp").document(phonenumber).set(docData);
			logger.info("OTP updated time : " + future.get().getUpdateTime());
			logger.info("Successfully updated the OTP status for the phonenumber in the db!");
		}
	}
}
