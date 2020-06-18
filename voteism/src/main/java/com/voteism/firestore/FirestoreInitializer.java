/*
 * Copyright 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.firestore;

import java.io.FileInputStream;
import java.net.URL;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.auth.oauth2.GoogleCredentials;

/**
 * Class to initialize the Firestore object
 * 
 * @author Ranjit Kollu
 *
 */
public class FirestoreInitializer {
	private static Firestore firestore = null;
	
	private FirestoreInitializer() {}
	
	/**
	 * Initialize and get the firestore instance 
	 * 
	 * @return Firestore instance
	 */
	public static Firestore getFirestoreInstance() {
		try {
			if (null == firestore) {
				URL fileUrl = FirestoreOptions.class.getClassLoader().getResource("voteism-service-account.json");
				FileInputStream serviceAccount = new FileInputStream(fileUrl.getFile());
				firestore = FirestoreOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(serviceAccount))
						.build().getService();
			}
			
			return firestore;
		} catch (Exception ex) {
			System.out.println(ex.getCause().getMessage());
			throw new RuntimeException(ex.getLocalizedMessage());
		}
	}
}
