/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.ses;

import com.voteism.aws.secretsmanager.VoteismSecretsManager;
import com.voteism.domain.User;
import com.voteism.utils.VoteismUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegation class for sending SMS using sendwithses application via REST API
 * 
 * @author Ranjit Kollu
 *
 */
public class SendWithSes {
	private static final Logger logger = LoggerFactory.getLogger(SendWithSes.class);

	/**
	 * Send OTP to the user using the sendwithses application
	 * 
	 * @param phonenumber phone to send the otp to
	 * @param otp OTP to send
	 */
	public static void sendSMSWithSes(final String phonenumber, final long otp) {
		final String msgString = String.format("OTP for VOTEism is %s. OTP expires in 5 minutes. Do not share OTP",
				otp);
		final String requestBody = String.format("{\"mobile\":\"%s\",\"message\":\"%s\"}", phonenumber, msgString);
		final String requestURI = String.format("https://api.sendwithses.com/send-sms");

		postUserToSendSES(requestURI, requestBody, VoteismSecretsManager.getSendWithSMSSesKey());
	}

	/**
	 * Post the API request to sendwithses application to send SMS or register the user
	 * 
	 * @param requestURI REST API to call
	 * @param requestBody Request body
	 * @param key API access key
	 */
	public static void postUserToSendSES(final String requestURI, final String requestBody, final String key) {
		HttpURLConnection conn = null;
		final Map<String, String> headers = new HashMap<>();

		headers.put("x-api-key", key);
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");

		try {
			URL url = new URL(requestURI);
			final URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
					url.getQuery(), url.getRef());
			url = uri.toURL();

			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");

			for (String headerKey : headers.keySet()) {
				conn.setRequestProperty(headerKey, headers.get(headerKey));
			}

			conn.setDoOutput(true);

			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				wr.writeBytes(requestBody);
				wr.flush();
			}

			final int responseCode = conn.getResponseCode();
			logger.info("POST Response Code from sendwithses :: " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				logger.info("Successfully registered with sendwithses system");
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				logger.info(response.toString());
			} else {
				logger.error("POST request not worked");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != conn) {
				conn.disconnect();
			}
		}
	}
}
