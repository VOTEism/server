/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.firebase;

import java.io.FileInputStream;
import java.net.URL;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Class to initialize the Firebase authentication object
 * 
 * @author Ranjit Kollu
 *
 */
public class FirebaseAuthInitializer {
	private static FirebaseAuth firebaseAuth = null;

	private FirebaseAuthInitializer() {
	}

	/**
	 * Initialize and get the firebase authentication instance 
	 * 
	 * @return Firebase authentication instance
	 */
	public static FirebaseAuth getFirebaseAuthInstance() {
		try {
			if (null == firebaseAuth) {
				URL fileUrl = FirebaseAuthInitializer.class.getClassLoader().getResource("voteism-service-account.json");
				FileInputStream serviceAccount = new FileInputStream(fileUrl.getFile());
				FirebaseOptions firebaseOptions = FirebaseOptions.builder()
						.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();
				FirebaseApp firebaseApp = FirebaseApp.initializeApp(firebaseOptions);
				firebaseAuth = FirebaseAuth.getInstance(firebaseApp);
			}

			return firebaseAuth;
		} catch (Exception ex) {
			System.out.println(ex.getCause().getMessage());
			throw new RuntimeException(ex.getLocalizedMessage());
		}
	}
}
