/*
 * Copyright 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */
package com.voteism.domain;

/**
 * Location class containing the location information
 * 
 * @author Ranjit Kollu
 *
 */
public class Location {
	private String city;
	private String state;
	private String country;
	private String current_latitude;
	private String current_longitude;
	
	/**
	 * Constructor
	 * 
	 * @param city city name
	 * @param state state name
	 * @param country country name
	 * @param current_latitude current latitude
	 * @param current_longitude current longitude
	 */
	public Location(String city, String state, String country, String current_latitude, String current_longitude) {
		this.city = city;
		this.state = state;
		this.country = country;
		this.current_latitude = current_latitude;
		this.current_longitude = current_longitude;
	}
	
	/**
	 * Set the city name
	 * 
	 * @param city city name
	 */
	public void setCity(String city) {
		this.city = city;
	}
	
	/**
	 * Get city name
	 * 
	 * @return city
	 */
	public String getCity() {
		return this.city;
	}
	
	/**
	 * Set the state name
	 * 
	 * @param state state name
	 */
	public void setState(String state) {
		this.state = state;
	}
	
	/**
	 * Get state name
	 * 
	 * @return state
	 */
	public String getState() {
		return this.state;
	}
	
	/**
	 * Set the country name
	 * 
	 * @param country country name
	 */
	public void setCountry(String country) {
		this.country = country;
	}
	
	/**
	 * Get country name
	 * 
	 * @return country
	 */
	public String getCountry() {
		return this.country;
	}
	
	/**
	 * Set the current latitude
	 * 
	 * @param latitude current latitude
	 */
	public void setCurrentLatitude(String latitude) {
		this.current_latitude = latitude;
	}
	
	/**
	 * Get current latitude
	 * 
	 * @return current latitude
	 */
	public String getCurrentLatitude() {
		return this.current_latitude;
	}
	
	/**
	 * Set the current longitude
	 * 
	 * @param longitude current longitude
	 */
	public void setCurrentLongitude(String longitude) {
		this.current_longitude = longitude;
	}
	
	/**
	 * Get current longitude
	 * 
	 * @return current longitude
	 */
	public String getCurrentLongitude() {
		return this.current_longitude;
	}
}
