/*
 * Copyright 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.aws.secretsmanager;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InternalServiceErrorException;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Secrets manager class for the voteism app
 * 
 * @author Ranjit Kollu
 *
 */
@Component
public class VoteismSecretsManager {
	public static String region_code = "foo";
	private static AWSSecretsManager awsSecretsClient = null;
	
	/**
	 * Set the region code
	 * 
	 * @param code Region code (Example US-east)
	 */
	@Value("${SECRETS_REGION}")
	public void setRegionCode(String code) {
		region_code = code;
	}
	
	/**
	 * Get the AWS secrets manager client
	 * 
	 * @return AWS secrets manager
	 */
	public static AWSSecretsManager getSecretClient() {
		if(null == awsSecretsClient) {
			// Create a Secrets Manager client
		    awsSecretsClient  = AWSSecretsManagerClientBuilder.standard()
		                                    .withRegion(region_code)
		                                    .build();
		}
		
		return awsSecretsClient;
	}
	
	/**
	 * Get the AWS secrets for the app as a Json node object
	 * 
	 * @return Json node object
	 */
	public static JsonNode getSecret() {
		final String secretName = "voteismsecrets";
	    String secret, decodedBinarySecret;
	    
	    GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
	                    .withSecretId(secretName);
	    GetSecretValueResult getSecretValueResult = null;

	    try {
	        getSecretValueResult = getSecretClient().getSecretValue(getSecretValueRequest);
	    } catch (DecryptionFailureException e) {
	        // Secrets Manager can't decrypt the protected secret text using the provided KMS key.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    } catch (InternalServiceErrorException e) {
	        // An error occurred on the server side.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    } catch (InvalidParameterException e) {
	        // You provided an invalid value for a parameter.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    } catch (InvalidRequestException e) {
	        // You provided a parameter value that is not valid for the current state of the resource.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    } catch (ResourceNotFoundException e) {
	        // We can't find the resource that you asked for.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    }
	    
	    ObjectMapper objectMapper = new ObjectMapper();
		JsonNode secretsJson = null;

		try {
		    // Decrypts secret using the associated KMS CMK.
		    // Depending on whether the secret is a string or binary, one of these fields will be populated.
		    if (getSecretValueResult.getSecretString() != null) {
		        secret = getSecretValueResult.getSecretString();
		        secretsJson = objectMapper.readTree(secret);
		    }
		    else {
		        decodedBinarySecret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
		        secretsJson = objectMapper.readTree(decodedBinarySecret);
		    }
		    
		    return secretsJson;
		} catch( JsonProcessingException ex) {
			throw new RuntimeException(ex.getLocalizedMessage());
		}
	}
	
	/**
	 * Get the Twilio Account ID
	 * 
	 * @return Twilio account id
	 */
	public static String getTwilioAccountId() {
		return getSecret().get("TWILIO_ACCOUNT_SID").asText();
	}
	
	/**
	 * Get the twilio account secret key
	 * 
	 * @return Twilio account secret key
	 */
	public static String getTwilioAccountKey() {
		return getSecret().get("TWILIO_AUTH_TOKEN").asText();
	}
	
	/**
	 * Get the send with ses API account secret key
	 * 
	 * @return send with ses account secret key
	 */
	public static String getSendWithSesKey() {
		return getSecret().get("SEND_WITH_SES_KEY").asText();
	}
	
	/**
	 * Get the send with ses API account sms secret key
	 * 
	 * @return send with ses account sms secret key
	 */
	public static String getSendWithSMSSesKey() {
		return getSecret().get("SEND_SMS_WITH_SES_KEY").asText();
	}
	
	/**
	 * Get the key for AWS API gateway
	 * 
	 * @return key for AWS API gateway
	 */
	public static String getApiKey() {
		return getSecret().get("X_API_KEY").asText();
	}
	
	/**
	 * Get the JWT token secret
	 * 
	 * @return JWT token secret 
	 */
	public static String getJwtSecret() {
		return getSecret().get("JWT_SECRET").asText();
	}
	
	/**
	 * Get the JWT token timeout
	 * 
	 * @return JWT token timeout
	 */
	public static long getJwtTimeout() {
		return getSecret().get("JWT_EXPIRATION").asLong();
	}
	
	/**
	 * Get the firebase account secret
	 * 
	 * @return firebase account secret
	 */
	public static String getFirebaseSecret() {
		return getSecret().get("FIREBASE_SECRET").asText();
	}
	
	/**
	 * Get voteism app public key
	 * 
	 * @return voteism app public key
	 */
	public static String getVoteismPublicKey() {
		return getSecret().get("VOTEISM_PUBLIC_KEY").asText();
	}
}
