/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */
package com.voteism.controllers;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.voteism.domain.User;
import com.voteism.exceptions.LoginException;
import com.voteism.exceptions.PhoneException;
import com.voteism.exceptions.UserException;
import com.voteism.properties.YAMLConfig;
import com.google.firebase.auth.FirebaseAuthException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.voteism.aop.SessionToken;
import com.voteism.aws.secretsmanager.VoteismSecretsManager;
import com.voteism.domain.OTPStatusEnum;
import com.voteism.domain.TokenQueueManager;
import com.voteism.security.JWTProvider;
import com.voteism.services.PhoneService;
import com.voteism.services.UserService;
import com.voteism.ses.SendWithSes;
import com.voteism.utils.VoteismUtils;

/**
 * Controller class for the User operations/end points
 * 
 * @author Ranjit Kollu
 *
 */
@Controller
public class UserController {
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private PhoneService phoneService;

	@Autowired
	private JWTProvider jwtProvider;

	@Autowired
	private YAMLConfig myConfig;
	
	@Value("${WHITE_LIST_PHONE_NUMBERS}")
	private List<String> whiteListPhonenumbers = new ArrayList<String>();

	/**
	 * User registration end point. Registration of the user is attempted by sending a OTP for the user to verify their registration
	 * 
	 * @param user User to register with the voteism app
	 * 
	 * @return Returns a map with the status of the OTP sent to the user(which is NOT_VERIFIED).
	 * @throws LoginException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/voteism/users/register", method = RequestMethod.POST)
	public Map<String, Object> registerUser(@RequestBody User user) throws LoginException {
		logger.info("Register user!");
		return this.userService.register(user);
	}

	/**
	 * User login end point. Login the user who has already successfully registered or login with numbers that are white listed (for testing purposes)
	 * 
	 * @param user User to login to the voteism app
	 * 
	 * @return Returns the user object along with the VOTEISM_ACCESS_TOKEN (this is returned only after a successful OTP verification)
	 * to be passed into as part of other requests, VOTEISM_FIRESTORE_TOKEN for the client application to be able access firestore database,
	 * VOTEISM_PUBLIC_KEY for the client application to be able to encrypt the user vote data, also we send the candiate 1&2 names, image and
	 * logo links
	 * 
	 * @throws LoginException
	 */
	@SuppressWarnings("unchecked")
	@SessionToken
	@RequestMapping(value = "/voteism/users/login", method = RequestMethod.POST)
	public ResponseEntity<?> loginUser(@RequestBody User user) throws Throwable {
		final AtomicReference<Throwable> thrownException = new AtomicReference<>(null);
		CompletableFuture<ResponseEntity<?>> resp = CompletableFuture.supplyAsync(() -> {
			try {
				final String token = TokenQueueManager.getUserRequestQueue().take();
				logger.info("Login user!");

				final String phonenumber = VoteismUtils.getPhonenumbeWithISDCode(user.getPhonenumber());
				final HttpHeaders headers = new HttpHeaders();
				Map<String, Object> retUserMap = new HashMap<String, Object>();
				
				//This piece of code is for logging white listed numbers for testing purpose.
				if (!userService.doesUserExist(phonenumber)) {
					if(whiteListPhonenumbers.contains(phonenumber)) {
						final User retUser = userService.addUser(user);
						retUserMap.put("user", retUser);
						headers.add("VOTEISM_TOKEN", myConfig.getGateway().getApiKey());
						headers.add("VOTEISM_ACCESS_TOKEN", jwtProvider.generateToken(phonenumber));
						headers.add("VOTEISM_FIRESTORE_TOKEN", jwtProvider.getFirebaseAccessToken());

						logger.info("User logged in successfully!");
						return new ResponseEntity<Map<String, Object>>(retUserMap, headers, HttpStatus.ACCEPTED);
					}
					
					logger.info("User does not exist!");
					final Map<String, Object> retMap = registerUser(user);
					logger.info("Completed registering the user!");
					return new ResponseEntity<Map<String, Object>>(retMap, HttpStatus.ACCEPTED);
				}

				retUserMap = this.userService.login(user);

				headers.add("VOTEISM_TOKEN", myConfig.getGateway().getApiKey());
				headers.add("VOTEISM_ACCESS_TOKEN", jwtProvider.generateToken(phonenumber));
				headers.add("VOTEISM_FIRESTORE_TOKEN", jwtProvider.getFirebaseAccessToken());
				retUserMap.put("candidates", VoteismUtils.getCannedCandidateInfo());
				
				logger.info("User logged in successfully!");
				return new ResponseEntity<Map<String, Object>>(retUserMap, headers, HttpStatus.ACCEPTED);
			} catch (LoginException | FirebaseAuthException | UserException | InterruptedException ex) {
				return new ResponseEntity<String>(ex.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}, Executors.newCachedThreadPool()).whenComplete((v, t) -> {
			if (t != null) {
				thrownException.set(t);
			}
		});

		try {
			return resp.get();
		} catch (CompletionException ex) {
			throw (Optional.ofNullable(thrownException.get()).orElse(ex));
		}
	}

	/**
	 * OTP related operations end point. The user enters the OTP from the client application, the requests come in to this end point which verifies it,
	 * the other use of this end point is to a request to resend the OTP.
	 * 
	 * @param otpMap Map describing the OTP operation (verify or resend)
	 * @return returns a map with the user object in the case of a success, along with the VOTEISM_ACCESS_TOKEN (this is returned only after a successful OTP verification)
	 * to be passed into as part of other requests, VOTEISM_FIRESTORE_TOKEN for the client application to be able access firestore database,
	 * VOTEISM_PUBLIC_KEY for the client application to be able to encrypt the user vote data, also we send the candiate 1&2 names, image and
	 * logo links, ther wise it returns a FAIL . In the case of a resend we return (NOT_VERIFIED).
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	@SessionToken
	@RequestMapping(value = "/voteism/otp", method = RequestMethod.POST)
	public ResponseEntity<?> otpOps(@RequestBody Map<String, Object> otpMap) throws Throwable {
		final AtomicReference<Throwable> thrownException = new AtomicReference<>(null);
		final CompletableFuture<ResponseEntity<?>> resp = CompletableFuture.supplyAsync(() -> {
			try {
				logger.info("OTP ops for voteism app!");

				Map<String, Object> retMap = null;
				final String token = TokenQueueManager.getUserRequestQueue().take();
				final String operation = (String) otpMap.get("operation");
				
				if (operation.equalsIgnoreCase("verify")) {
					logger.info("Verifying the OTP for the user...");
					final long otp = Long.parseLong((String) otpMap.get("otp"));

					final GsonBuilder gsonMapBuilder = new GsonBuilder();
					final Gson gsonObject = gsonMapBuilder.create();
					final String jsonString = gsonObject.toJson(otpMap.get("user"));
					final User user = gsonObject.fromJson(jsonString, User.class);
					
					final String phonenumber = VoteismUtils.getPhonenumbeWithISDCode(user.getPhonenumber());

					logger.info("Converted the json string to user!");

					retMap = phoneService.verifyOTP(phonenumber, otp);
					final String otpStatus = (String) retMap.get("otpstatus");

					if (otpStatus.equalsIgnoreCase(OTPStatusEnum.OK.name())) {
						logger.info("Successfully verified the OTP for the user...");

						final User retUser = userService.addUser(user);
						retMap.put("user", retUser);

						final HttpHeaders headers = new HttpHeaders();

						headers.add("VOTEISM_TOKEN", myConfig.getGateway().getApiKey());
						headers.add("VOTEISM_ACCESS_TOKEN", jwtProvider.generateToken(phonenumber));
						headers.add("VOTEISM_FIRESTORE_TOKEN",
								jwtProvider.getFirebaseAccessToken());
						
						retMap.put("candidates", VoteismUtils.getCannedCandidateInfo());
						return new ResponseEntity<Map<String, Object>>(retMap, headers, HttpStatus.ACCEPTED);
					} else {
						throw new InvalidKeyException("OTP you entered is invalid or expired, please try again!");
					}
				} else if (operation.equalsIgnoreCase("resend")) {
					logger.info("Resending the OTP for the user...");

					final String phonenumber =
							VoteismUtils.getPhonenumbeWithISDCode((String) otpMap.get("phonenumber"));
					retMap = phoneService.resendOTP(phonenumber);
				}

				return new ResponseEntity<Map<String, Object>>(retMap, HttpStatus.ACCEPTED);
			} catch (PhoneException | FirebaseAuthException | InvalidKeyException | UserException
					| InterruptedException ex) {
				return new ResponseEntity<String>(ex.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}).whenComplete((v, t) -> {
			if (t != null) {
				thrownException.set(t);
			}
		});

		try {
			return resp.get();
		} catch (CompletionException ex) {
			throw (Optional.ofNullable(thrownException.get()).orElse(ex));
		}
	}

	/**
	 * Logout the user
	 * 
	 * @return logout and return the Http status
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	@SessionToken
	@RequestMapping(value = "/voteism/users/logout", method = RequestMethod.POST)
	public ResponseEntity<?> logoutUser() throws Throwable {
		final AtomicReference<Throwable> thrownException = new AtomicReference<>(null);
		final CompletableFuture<ResponseEntity<String>> resp = CompletableFuture.supplyAsync(() -> {
			try {
				final String phonenumber = TokenQueueManager.getUserRequestQueue().take();
				logger.info("Logout user");
				this.userService.logout(phonenumber);
				logger.info("User logged out successfully!");
				return new ResponseEntity<String>("User successfully logged out!", HttpStatus.ACCEPTED);
			} catch (InterruptedException | UserException ex) {
				throw new RuntimeException(ex.getLocalizedMessage());
			}
		}).whenComplete((v, t) -> {
			if (t != null) {
				thrownException.set(t);
			}
		});

		try {
			return resp.get();
		} catch (CompletionException ex) {
			throw (Optional.ofNullable(thrownException.get()).orElse(ex));
		}
	}

	/**
	 * Refresh the firestore token if the client determines if it expired.
	 * 
	 * @return Map containing the firestore token.
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	@SessionToken
	@RequestMapping(value = "/voteism/users/refresh/firestoretoken", method = RequestMethod.POST)
	public ResponseEntity<Map<String, String>> refreshFirestoreToken() throws Throwable {
		final AtomicReference<Throwable> thrownException = new AtomicReference<>(null);
		CompletableFuture<ResponseEntity<Map<String,String>>> resp = CompletableFuture.supplyAsync(() -> {
			try {
				final String phonenumber = VoteismUtils.getPhonenumbeWithISDCode(TokenQueueManager.getUserRequestQueue().take());
				final Map<String, String> responseMap = new HashMap<String,String>();
				responseMap.put("firestoretoken", jwtProvider.getFirebaseAccessToken());
				return new ResponseEntity<Map<String, String>>(responseMap, HttpStatus.OK);
			} catch (FirebaseAuthException | InterruptedException ex) {
				throw new RuntimeException(ex.getLocalizedMessage());
			}
		}).whenComplete((v, t) -> {
			if (t != null) {
				thrownException.set(t);
			}
		});

		try {
			return resp.get();
		} catch (CompletionException ex) {
			throw (Optional.ofNullable(thrownException.get()).orElse(ex));
		}
	}
	
	/**
	 * Fetch the public key for voteism app
	 * 
	 * @return Map containing the public key
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	@SessionToken
	@RequestMapping(value = "/voteism/fetch/publickey", method = RequestMethod.GET)
	public ResponseEntity<Map<String, String>> getVoteismPublicKey() throws Throwable {
		final AtomicReference<Throwable> thrownException = new AtomicReference<>(null);
		CompletableFuture<ResponseEntity<Map<String,String>>> resp = CompletableFuture.supplyAsync(() -> {
			try {
				final String phonenumber = TokenQueueManager.getUserRequestQueue().take();
				final Map<String, String> responseMap = new HashMap<String,String>();
				responseMap.put("publickey", VoteismSecretsManager.getVoteismPublicKey());
				return new ResponseEntity<Map<String, String>>(responseMap, HttpStatus.OK);
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex.getLocalizedMessage());
			}
		}).whenComplete((v, t) -> {
			if (t != null) {
				thrownException.set(t);
			}
		});

		try {
			return resp.get();
		} catch (CompletionException ex) {
			throw (Optional.ofNullable(thrownException.get()).orElse(ex));
		}
	}
}
