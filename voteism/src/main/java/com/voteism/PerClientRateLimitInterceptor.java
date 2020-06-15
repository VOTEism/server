/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;

/**
 * Throttling class for the number of vote requests per user per minute
 * 
 * @author Ranjit Kollu
 *
 */
@Component
public class PerClientRateLimitInterceptor implements HandlerInterceptor {
	@Value("${VOTE_RATE}")
	public static int vote_rate;
	
	private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
	
	/**
	 * Set the throttling rate for voting per user
	 * 
	 * @param rate
	 */
	@Value("${VOTE_RATE}")
	public void setVoteRate(int rate) {
		vote_rate = rate;
	}

	/**
	 * Handle the request to apply throttling
	 * 
	 * @param request Servlet request
	 * @param response Servlet response
	 * @param handler handler for the requests
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		Bucket requestBucket = null;

		String apiKey = request.getHeader("VOTEISM_ACCESS_TOKEN");
		if (apiKey != null && !apiKey.isEmpty()) {
			requestBucket = this.buckets.computeIfAbsent(apiKey, key -> rateBucket());
		}

		if(null == requestBucket) {
			return false;
		}
		
		ConsumptionProbe probe = requestBucket.tryConsumeAndReturnRemaining(1);
		
		if (probe.isConsumed()) {
			return true;
		}

		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
		return false;
	}

	/**
	 * Create the bucket for throttling
	 * 
	 * @return Bucket that contains the tokens to apply throttling
	 */
	private static Bucket rateBucket() {
		return Bucket4j.builder().addLimit(Bandwidth.classic(vote_rate, Refill.intervally(vote_rate, Duration.ofMinutes(1)))).build();
	}
}
