/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.domain;

/**
 * User vote class containing the vote information of the user
 * 
 * @author Ranjit Kollu
 *
 */
public class UserVote {
	private String encryptedVote;
	private String voteSignature;
	private String votedTime;
	private String timezone;
	private String gmtTime;
	private User user;
	
	/**
	 * Constructor
	 * 
	 * @param encryptedVote Encrypted vote information
	 * @param voteSignature Signature of the vote
	 * @param votedTime voted time
	 * @param gmtTime GMT time the user voted
	 * @param user User who voted
	 */
	public UserVote(String encryptedVote, String voteSignature,String votedTime, String gmtTime, User user) {
		this.encryptedVote = encryptedVote;
		this.voteSignature = voteSignature;
		this.votedTime = votedTime;
		this.gmtTime = gmtTime;
		this.user = user;
	}

	/**
	 * Set the encrypted vote
	 * 
	 * @param encryptedVote
	 */
	public void setEncryptedVote(String encryptedVote) {
		this.encryptedVote = encryptedVote;
	}
	
	/**
	 * Get the encryted vote
	 * 
	 * @return encryptedVote
	 */
	public String getEncryptedVote() {
		return this.encryptedVote;
	}
	
	/**
	 * Set the vote signature
	 * 
	 * @param voteSignature
	 */
	public void setVoteSignature(String voteSignature) {
		this.voteSignature = voteSignature;
	}
	
	/**
	 * Get the vote signature
	 * 
	 * @return voteSignature
	 */
	public String getVoteSignature() {
		return this.voteSignature;
	}
	
	/**
	 * Set the user information who voted
	 * 
	 * @param user
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * Get the user who voted
	 * 
	 * @return user
	 */
	public User getUser() {
		return this.user;
	}
	
	/**
	 * Set the voted time
	 * 
	 * @param votedTime
	 */
	public void setVotedTime(String votedTime) {
		this.votedTime = votedTime;
	}
	
	/**
	 * Get the voted time
	 * 
	 * @return votedTime
	 */
	public String getVotedTime() {
		return this.votedTime;
	}
	
	/**
	 * Set the timezone where user voted
	 * 
	 * @param timezone
	 */
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	
	/**
	 * Get the timezone where user voted
	 * 
	 * @return timezone
	 */
	public String getTimezone() {
		return this.timezone;
	}
	
	/**
	 * Set the GMT time the user voted
	 * 
	 * @param gmtTime
	 */
	public void setGMTtime(String gmtTime) {
		this.gmtTime = gmtTime;
	}
	
	/**
	 * Get the GMT time the user voted
	 * 
	 * @return gmtTime
	 */
	public String getGMTtime() {
		return this.gmtTime;
	}
}
