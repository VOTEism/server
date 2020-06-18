/*
 * Copyright 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.voteism.aws.secretsmanager.VoteismSecretsManager;

/**
 * Voteism application properties read from the properties YAML file
 * 
 * @author Ranjit Kollu
 *
 */
@Configuration
@EnableConfigurationProperties
public class YAMLConfig {
	private Gateway gateway = new Gateway();
	
	/**
	 * Get the AWS API gateway
	 * 
	 * @return Gateway
	 */
	public Gateway getGateway() {
		return this.gateway;
	}
	
	/**
	 * Set the API gateway
	 * 
	 * @param gateway
	 */
	public void setGateway(Gateway gateway) {
		this.gateway = gateway;
	}
	
	/**
	 * AWS API gateway class
	 * 
	 * @author Ranjit Kollu
	 *
	 */
	public class Gateway {	
		/**
		 * Get the AWS API gateway API ke
		 *
		 * @return API key read from the secrets manager
		 */
		public String getApiKey() {
			return VoteismSecretsManager.getApiKey();
		}
	}
}

