/*
 * Copyright 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.voteism.aws.secretsmanager.VoteismSecretsManager;
import com.voteism.firebase.FirebaseAuthInitializer;

/**
 * Class to generate the JWT token, verify the token and retrieve the user information from the token.
 * This class uses the Auth0 library
 * 
 * @author Ranjit Kollu
 *
 */
@Component
public class JWTProvider {
	/**
	 * Generate the JWT token containing the user phone number
	 * 
	 * @param phonenumber
	 * @return Token
	 */
	public String generateToken(String phonenumber) {
		// HMAC
		final Algorithm algorithm = Algorithm.HMAC256(VoteismSecretsManager.getJwtSecret());
		String token = null;
		final Date now = new Date();
		final Date expiryDate = new Date(now.getTime() + VoteismSecretsManager.getJwtTimeout());

		token = JWT.create().withIssuer("auth0").withClaim("user", phonenumber).withIssuedAt(new Date())
				.withExpiresAt(expiryDate).sign(algorithm);

		return token;
	}

	/**
	 * Verify and validate the token passed in by the user request
	 * 
	 * @param token
	 * @return True if the verification passes otherwise false
	 */
	public boolean verifyToken(String token) {
		try {
			final Algorithm algorithm = Algorithm.HMAC256(VoteismSecretsManager.getJwtSecret());
			final JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build(); // Reusable verifier
																								// instance
			verifier.verify(token);
			return true;
		} catch (JWTDecodeException exception) {
			return false;
		} catch (JWTVerificationException ex) {
			return false;
		}
	}

	/**
	 * Get the user phone number from the token
	 * 
	 * @param token
	 * @return User phone number
	 */
	public String getUserPhonenumber(String token) {
		String userPhonenumber = "";
		try {
			final Algorithm algorithm = Algorithm.HMAC256(VoteismSecretsManager.getJwtSecret());
			final JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build(); // Reusable verifier
																								// instance
			final DecodedJWT decodedJWT = verifier.verify(token);
			userPhonenumber = decodedJWT.getClaim("user").asString();
		} catch (JWTDecodeException exception) {
		} catch (JWTVerificationException ex) {
		}
		return userPhonenumber;
	}

	/**
	 * Get the firebase authentication custom access token
	 * 
	 * @return custom token to be used for firebase authentication
	 * @throws FirebaseAuthException
	 */
	public String getFirebaseAccessToken() throws FirebaseAuthException {
		final FirebaseAuth firebaseAuth = FirebaseAuthInitializer.getFirebaseAuthInstance();
		final String uid = "user";
		final Map<String, Object> additionalClaims = new HashMap<String, Object>();
		additionalClaims.put("premiumAccount", true);

		return firebaseAuth.createCustomToken(uid, additionalClaims);
	}
}
