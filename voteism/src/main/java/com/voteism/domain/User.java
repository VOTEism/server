/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.domain;

import java.util.Date;

/**
 * User class containing the user information
 * 
 * @author Ranjit Kollu
 *
 */
public class User {	
	private String phonenumber;
	private Date lastLoginTime;
	private boolean loginStatus;
	private Location registeredLocation;
	private Location location;
	private UserDeviceDetails userDeviceDetails;
	
	/**
	 * Constructor
	 * 
	 * @param phonenumber phone number
	 * @param lastlogintime last login time of the user
	 * @param loginStatus login status of the user (SUCCESS/FAIL)
	 * @param registeredLocation registered location of the user
	 * @param location current location of the user
	 * @param devicedetails Device details of the user
	 */
	public User(String phonenumber, Date lastlogintime, boolean loginStatus, Location registeredLocation, Location location, 
			UserDeviceDetails devicedetails) {
		this.phonenumber = phonenumber;
		this.lastLoginTime = lastlogintime;
		this.loginStatus = loginStatus;
		this.registeredLocation = registeredLocation;
		this.location = location;
		this.userDeviceDetails = devicedetails;
	}
	
	/**
	 * Get the user phone number
	 * 
	 * @return phonenumber
	 */
	public String getPhonenumber() {
		return this.phonenumber;
	}
	
	/**
	 * Set the phone number
	 * 
	 * @param phonenumber
	 */
	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}
	
	/**
	 * Get the last login time of the user
	 * 
	 * @return lastlogintime
	 */
	public Date getLastLogintime() {
		return this.lastLoginTime;
	}
	
	/**
	 * Set last login time of user
	 * 
	 * @param lastloginTime
	 */
	public void setLastLoginTime(Date lastloginTime) {
		this.lastLoginTime = lastloginTime;
	}
	
	/**
	 * Get login status of the user
	 * 
	 * @return loginStatus
	 */
	public boolean getLoginStatus() {
		return this.loginStatus;
	}
	
	/**
	 * Set login status of the user
	 * 
	 * @param loginStatus
	 */
	public void setLoginStatus(boolean loginStatus) {
		this.loginStatus = loginStatus;
	}
	
	/**
	 * Get the registered location of the user
	 * 
	 * @return registeredLocation
	 */
	public Location getRegisteredLocation() {
		return this.registeredLocation;
	}
	
	/**
	 * Set the registered location of the user
	 * 
	 * @param registeredLocation
	 */
	public void setRegisteredLocation(Location registeredLocation) {
		this.registeredLocation = registeredLocation;
	}
	
	/**
	 * Get the current location of the user
	 * 
	 * @return location
	 */
	public Location getLocation() {
		return this.location;
	}
	
	/**
	 * Set the current location of the user
	 * 
	 * @param location
	 */
	public void setLocation(Location location) {
		this.location = location;
	}
	
	/**
	 * Get User device details
	 * 
	 * @return userDeviceDetails
	 */
	public UserDeviceDetails getUserDeviceDetails() {
		return this.userDeviceDetails;
	}
	
	/**
	 * Set the user device details
	 * 
	 * @param deviceDetails
	 */
	public void setUserDeviceDetails(UserDeviceDetails deviceDetails) {
		this.userDeviceDetails = deviceDetails;
	}
}
