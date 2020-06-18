/*
 * Copyright 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.bigquery;

import java.io.FileInputStream;
import java.net.URL;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.auth.oauth2.GoogleCredentials;

/**
 * Class to initialize the Big query object
 * 
 * @author Ranjit Kollu
 *
 */
public class BigQueryInitializer {
	private static BigQuery bigQuery = null;
	
	private BigQueryInitializer() {}
	
	/**
	 * Initialize and get the big query instance 
	 * 
	 * @return Big query instance
	 */
	public static BigQuery getBigQueryInstance() {
		try {
			if (null == bigQuery) {
				URL fileUrl = BigQueryOptions.class.getClassLoader().getResource("voteism-service-account.json");
				FileInputStream serviceAccount = new FileInputStream(fileUrl.getFile());
				bigQuery = BigQueryOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(serviceAccount))
						.build().getService();
			}
			
			return bigQuery;
		} catch (Exception ex) {
			System.out.println(ex.getCause().getMessage());
			throw new RuntimeException(ex.getLocalizedMessage());
		}
	}
}
