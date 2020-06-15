/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.aop;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.voteism.domain.TokenQueueManager;
import com.voteism.exceptions.LoginException;
import com.voteism.security.JWTProvider;
import com.voteism.utils.VoteismUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Annotation definition for the user token passed in for the application
 * 
 * @author Ranjit Kollu
 *
 */
@Aspect
@Component
public class SessionTokenAspect {
	@Autowired
	JWTProvider jwtProvider;

	private TokenQueueManager queueManager;
	private static final Logger logger = LoggerFactory.getLogger(SessionTokenAspect.class);

	/**
	 * Set the token queue manager
	 * 
	 * @param queueManager Token queue manager
	 */
	@Autowired
	public void setTokenQueueManager(TokenQueueManager queueManager) {
		this.queueManager = queueManager;
	}

	
	/**
	 * Cross point (Aspect oriented programming) for intercepting the call for a method that declares the SessionToken annotation.
	 * Once the token comes in from the user request, we find the user from the token and get his/her phone number and
	 * push it on to a block queue managed by the TokenQueueManager class.
	 * 
	 * @param joinPoint Join point for the annotation
	 * 
	 * @return Return the token
	 * @throws Throwable
	 */
	@Around("@annotation(SessionToken)")
	public Object checkIfTokenExists(ProceedingJoinPoint joinPoint) throws Throwable {
		final AtomicReference<Throwable> thrownException = new AtomicReference<>(null);
		final CompletableFuture<Object> resp = CompletableFuture.supplyAsync(() -> {
			String userPhonenumber;
			try {
				final String token = TokenQueueManager.getUserTokenQueue().take();
				VoteismUtils.loadStatenames();

				if (token.equalsIgnoreCase("DUMMY")) {
					TokenQueueManager.insertRequestUser("");
					userPhonenumber = "";
				} else {
					logger.info("Verifying token!");
					if (!jwtProvider.verifyToken(token)) {
						logger.info("Logout and login again to continue!");
						throw new LoginException("Logout and login again to continue!");
					} else {
						userPhonenumber = jwtProvider.getUserPhonenumber(token);
						TokenQueueManager.insertRequestUser(jwtProvider.getUserPhonenumber(token));
					}
				}

				return userPhonenumber;
			} catch (LoginException | ExecutionException ex) {
				throw new CompletionException(ex);
			} 
			catch (InterruptedException ex) {
				throw new RuntimeException(ex.getLocalizedMessage());
			}
		}).thenApplyAsync(userId -> {
			try {
				return joinPoint.proceed();
			} catch (Throwable ex) {
				throw new RuntimeException(ex.getLocalizedMessage());
			}
		}).whenComplete((v, t) -> {
            if (t != null) {
                thrownException.set(t);
            }
        });
		
		try {
			return resp.get();
		} catch(CompletionException ex) {
			throw (Optional.ofNullable(thrownException.get().getCause()).orElse(ex));
		}
	}
}
