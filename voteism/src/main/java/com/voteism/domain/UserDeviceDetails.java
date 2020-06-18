/*
 * Copyright 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * User device details class
 * 
 * @author Ranjit Kollu
 *
 */
@JsonIgnoreProperties({"ipaddress"})
public class UserDeviceDetails {
	private String deviceToken;
	private String deviceOS;
	private boolean simulator;
	private String macAddress;
	public String ipAddress;
	
	/**
	 * Constructor
	 * 
	 * @param deviceToken Device token
	 * @param deviceOS Device OS
	 * @param isSimulator is the device a simulator
	 * @param macAddress MAC address of the device
	 * @param ipAddress IP address of the device
	 */
	public UserDeviceDetails(String deviceToken, String deviceOS, boolean isSimulator, String macAddress, String ipAddress) {
		this.deviceToken = deviceToken;
		this.deviceOS = deviceOS;
		this.simulator = isSimulator;
		this.macAddress = macAddress;
		this.ipAddress = ipAddress;
	}
	
	/**
	 * Get Device token
	 * 
	 * @return deviceToken
	 */
	public String getDeviceToken() {
		return this.deviceToken;
	}
	
	/**
	 * Set the device token
	 * 
	 * @param token
	 */
	public void setDeviceToken(String token) {
		this.deviceToken = token;
	}
	
	/**
	 * Get the Device OS
	 * 
	 * @return deviceOS
	 */
	public String getDeviceOS() {
		return this.deviceOS;
	}
	
	/**
	 * Set the device OS
	 * 
	 * @param os
	 */
	public void setDeviceOS(String os) {
		this.deviceOS = os;
	}
	
	/**
	 * Is the device a simulator
	 * 
	 * @return true or false
	 */
	public boolean isSimulator() {
		return this.simulator;
	}
	
	/**
	 * Set if the device is simulator or not
	 * 
	 * @param simulator
	 */
	public void setSimulator(boolean simulator) {
		this.simulator = simulator;
	}
	
	/**
	 * Get the MAC address of the device
	 * 
	 * @return macAddress
	 */
	public String getMACAddress() {
		return this.macAddress;
	}
	
	/**
	 * Set the MAC address of the device
	 * 
	 * @param macAddress
	 */
	public void setMACAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	
	/**
	 * Get the IP Address of the device
	 * 
	 * @return ipAddress
	 */
	public String getIPAddress() {
		return this.ipAddress;
	}
	
	/**
	 * Set the IP address of the device
	 * 
	 * @param ipAddress
	 */
	public void setIPAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
}
	
