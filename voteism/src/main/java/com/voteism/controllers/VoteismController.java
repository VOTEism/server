/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */
package com.voteism.controllers;

import java.text.ParseException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.voteism.aop.SessionToken;
import com.voteism.domain.TokenQueueManager;
import com.voteism.domain.UserVote;
import com.voteism.exceptions.VoteException;
import com.voteism.services.VoteService;

/**
 * Controller class for the Vote operations/end points
 * 
 * @author Ranjit Kollu
 *
 */
@Controller
public class VoteismController {
	private static final Logger logger = LoggerFactory.getLogger(VoteismController.class);
	
	@Autowired
	VoteService voteService;
	
	/**
	 * End point to save the user vote
	 * 
	 * @param userVote userVote object containing the encrypted vote data
	 * @return Send the success string otherwise return failure
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	@SessionToken
	@RequestMapping(value = "/voteism/users/vote", method = RequestMethod.POST)
	public ResponseEntity<?> saveUserVote(@RequestBody UserVote userVote) throws Throwable {
		final AtomicReference<Throwable> thrownException = new AtomicReference<>(null);
		final CompletableFuture<ResponseEntity<String>> resp = CompletableFuture.supplyAsync(() -> {
			try {
				final String userPhonenumber = TokenQueueManager.getUserRequestQueue().take();
				logger.info("Saving the user vote data");
				
				this.voteService.saveUserVote(userVote);
				logger.info("User vote data saved successfully!");
				StringBuilder outputMsg = new StringBuilder("You have cast your vote successfully.");
				
				outputMsg.append(System.lineSeparator())
						.append("You can change your vote anytime.")
						.append(System.lineSeparator())
						.append("Only your latest vote will be counted.")
						.append(System.lineSeparator())
						.append("Results announced twice a day.");
				
				return new ResponseEntity<String>(outputMsg.toString(), HttpStatus.ACCEPTED);
			} catch (InterruptedException | ParseException | VoteException | ExecutionException ex) {
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
}
