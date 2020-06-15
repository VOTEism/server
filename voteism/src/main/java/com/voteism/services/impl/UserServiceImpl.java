/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.services.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.i18n.phonenumbers.NumberParseException;

import com.voteism.domain.User;
import com.voteism.exceptions.LoginException;
import com.voteism.exceptions.PhoneException;
import com.voteism.exceptions.UserException;
import com.voteism.firestore.FirestoreInitializer;
import com.voteism.services.PhoneService;
import com.voteism.services.UserService;
import com.voteism.utils.VoteismUtils;

/**
 * Implementation class for Phone operations
 * 
 * @author Ranjit Kollu
 *
 */
@Component
public class UserServiceImpl implements UserService {
	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Autowired
	private PhoneService phoneService;
	
	@Value("${WHITE_LIST_PHONE_NUMBERS}")
	private List<String> whiteListPhonenumbers = new ArrayList<String>();
	
	@Value("${DEFAULT_LOGIN_NUMBERS}")
	private List<String> defaultLoginPhonenumbers = new ArrayList<String>();
	
	public boolean doesUserExist(final String phonenumber) throws UserException {
		logger.info("Checking if the user exists...");
		try {
			final Firestore firestore = FirestoreInitializer.getFirestoreInstance();
			final DocumentReference docRef = firestore.collection("users").document(phonenumber);
			final ApiFuture<DocumentSnapshot> future = docRef.get();
			final DocumentSnapshot document = future.get();
			
			return document.exists();
		} catch (ExecutionException | InterruptedException ex) {
			throw new UserException(ex.getLocalizedMessage());
		}
	}

	public synchronized Map<String, Object> register(final User user) throws LoginException {
		logger.info("Registering the user...");
		
		if(user.getUserDeviceDetails().isSimulator()) {
			throw new LoginException(
					"You cannot login to the app via the simulator!");
		}
		
		String phonenumber = VoteismUtils.getPhonenumbeWithISDCode(user.getPhonenumber());
		
		try {
			if (!phoneService.isMobile(phonenumber)) {
				throw new LoginException(
						"This is not a mobile phone number, please enter a valid mobile phone number!");
			}

			return phoneService.generateOTPAndSendSMS(phonenumber);
		} catch (PhoneException | NumberParseException ex) {
			throw new LoginException(ex.getLocalizedMessage());
		}
	}

	public synchronized Map<String, Object> login(final User user) throws UserException, LoginException {
		try {
			logger.info("Logging in the user...");
			
			String phonenumber = VoteismUtils.getPhonenumbeWithISDCode(user.getPhonenumber());
					
			if(!whiteListPhonenumbers.contains(phonenumber) && user.getUserDeviceDetails().isSimulator()) {
				throw new LoginException(
						"You cannot login to the app via the simulator!");
			}
			
			final Firestore firestore = FirestoreInitializer.getFirestoreInstance();
			final DocumentReference docRef = firestore.collection("users").document(phonenumber);

			final ApiFuture<DocumentSnapshot> future = docRef.get();
			final DocumentSnapshot document = future.get();

			final String deviceId = document.getString("device_id");
			final String macaddress = document.getString("macaddress");
			
			if(!(whiteListPhonenumbers.contains(phonenumber) || defaultLoginPhonenumbers.contains(phonenumber))
					&& !deviceId.equalsIgnoreCase(user.getUserDeviceDetails().getDeviceToken())) {
				return phoneService.generateOTPAndSendSMS(phonenumber);
			}
							
			Map<String, Object> retMap = new HashMap<String, Object>();
			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yy, HH:mm:ss.SSS a");
			final LocalDateTime now = LocalDateTime.now();
			final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy, HH:mm:ss.SSS a", Locale.ENGLISH);
			formatter.setTimeZone(TimeZone.getTimeZone(VoteismUtils.getTimezoneString()));
			final Date date = formatter.parse(dtf.format(now));

			final Map<String, Object> docData = new HashMap<>();
			docData.put("lastlogintime", date);			
			docData.put("device_id", user.getUserDeviceDetails().getDeviceToken());
			docData.put("device_os", user.getUserDeviceDetails().getDeviceOS());
			docData.put("is_simulator", user.getUserDeviceDetails().isSimulator());
			docData.put("macaddress", user.getUserDeviceDetails().getMACAddress());
			docData.put("ipAddress", user.getUserDeviceDetails().getIPAddress());	
			docData.put("loginStatus", true);

			final ApiFuture<WriteResult> futureWrite = docRef.set(docData);
			logger.info("Update time : " + futureWrite.get().getUpdateTime());
			
			logger.info("Logging in the user successfully!");
			user.setLastLoginTime(date);
			user.setLoginStatus(true);
			user.setPhonenumber(phonenumber);
			
			retMap.put("user", user);
			return retMap;
		} catch (ExecutionException | InterruptedException | ParseException | PhoneException ex) {
			throw new UserException(ex.getLocalizedMessage());
		}
	}

	public User addUser(final User user) throws UserException {
		logger.info("Adding the user");
		
		try {
			final Firestore firestore = FirestoreInitializer.getFirestoreInstance();
			final String phonenumber = VoteismUtils.getPhonenumbeWithISDCode(user.getPhonenumber());

			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yy, HH:mm:ss.SSS a");
			final LocalDateTime now = LocalDateTime.now();
			final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy, HH:mm:ss.SSS a", Locale.ENGLISH);
			formatter.setTimeZone(TimeZone.getTimeZone(VoteismUtils.getTimezoneString()));
			final Date date = formatter.parse(dtf.format(now));

			final Map<String, Object> docData = new HashMap<>();
			docData.put("lastlogintime", date);			
			docData.put("device_id", user.getUserDeviceDetails().getDeviceToken());
			docData.put("device_os", user.getUserDeviceDetails().getDeviceOS());
			docData.put("is_simulator", user.getUserDeviceDetails().isSimulator());
			docData.put("macaddress", user.getUserDeviceDetails().getMACAddress());
			docData.put("ipAddress", user.getUserDeviceDetails().getIPAddress());
			
			docData.put("loginStatus", true);

			final ApiFuture<WriteResult> futureWrite = firestore.collection("users").document(phonenumber).set(docData);
			logger.info("Update time : {} ", futureWrite.get().getUpdateTime());

			user.setPhonenumber(phonenumber);
			user.setLastLoginTime(date);
			user.setLoginStatus(true);
			
			logger.info("Successfully added the user!");
			return user;
		} catch (ExecutionException | InterruptedException | ParseException ex) {
			throw new UserException(ex.getLocalizedMessage());
		}
	}

	public void logout(final String phonenumber) throws UserException {
		try {
			logger.info("Logging out the user...");
			
			final Firestore firestore = FirestoreInitializer.getFirestoreInstance();
			final DocumentReference docRef = firestore.collection("users").document(phonenumber);

			final ApiFuture<DocumentSnapshot> future = docRef.get();
			final DocumentSnapshot document = future.get();

			final String city = document.getString("city");
			final String state = document.getString("state");
			final String country = document.getString("country");
			final String current_latitude = document.getString("current_latitude");
			final String current_longitude = document.getString("current_longitude");
			
			final String deviceId = document.getString("device_id");
			final String deviceOS = document.getString("device_os");
			final boolean isSimulator = document.getBoolean("is_simulator");
			final String macaddress = document.getString("macaddress");
			final String ipAddress = document.getString("ipAddress");
			
			final Date lastlogintime = document.getDate("lastlogintime");

			final Map<String, Object> docData = new HashMap<>();
			docData.put("lastlogintime", lastlogintime);
			docData.put("city", city);
			docData.put("state", state);
			docData.put("country", country);

			docData.put("current_latitude", current_latitude);
			docData.put("current_longitude", current_longitude);
			
			docData.put("device_id", deviceId);
			docData.put("device_os", deviceOS);
			docData.put("is_simulator", isSimulator);
			docData.put("macaddress", macaddress);
			docData.put("ipAddress", ipAddress);
			
			docData.put("loginStatus", false);

			final ApiFuture<WriteResult> futureWrite = docRef.set(docData);
			logger.info("Update time : {}", futureWrite.get().getUpdateTime());
			logger.info("Successfully logged out the user...");
		} catch (ExecutionException | InterruptedException ex) {
			throw new UserException(ex.getLocalizedMessage());
		}
	}
}
