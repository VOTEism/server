/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.voteism.bigquery.BigQueryInitializer;
import com.voteism.firestore.FirestoreInitializer;

/**
 * Main application class
 * 
 * @author Ranjit Kollu
 *
 */
@SpringBootApplication
public class VoteismApplication extends SpringBootServletInitializer implements WebMvcConfigurer {
	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(VoteismApplication.class, args);
		FirestoreInitializer.getFirestoreInstance();
		BigQueryInitializer.getBigQueryInstance();
	}

	/**
	 * Interceptor for the end point for the user vote and the throttling algorithm that is applied for the end point
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new PerClientRateLimitInterceptor()).addPathPatterns("/voteism/users/vote");
	}
}
