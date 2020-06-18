/*
 * Copyright 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.services;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.voteism.domain.UserVote;
import com.voteism.exceptions.VoteException;

public interface VoteService {
	/**
	 * Save the encrypted user vote
	 * 
	 * @param userVote User vote to save
	 * @throws VoteException
	 * @throws ParseException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void saveUserVote(final UserVote userVote) throws VoteException, ParseException, InterruptedException, ExecutionException;
}
