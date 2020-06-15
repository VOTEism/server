/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.voteism.domain.TokenQueueManager;
import com.voteism.security.JWTProvider;

/**
 * Class that gets the HTTP request from the user, and guarantees to be executed at least once per request.
 * 
 * @author Ranjit Kollu
 *
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	JWTProvider jwtProvider;
	
	/**
	 * Get the JWT Token that has the access token for the user
	 * 
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @param filterChain filter chain
	 * @throws ServletException, IOException
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		final CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
			try {
				setJwtToken(request);
				filterChain.doFilter(request, response);
			} 
			catch(Exception ex) {
				throw new CompletionException(ex);
			}
		});
		
		future.join();
	}

	/**
	 * Set the JWT Access Token for the user
	 * 
	 * @param request HttpServletRequest
	 * @throws IOException
	 */
	private void setJwtToken(HttpServletRequest request) throws IOException {
		final String voteismToken = request.getHeader("VOTEISM_ACCESS_TOKEN");
		try {
			if (voteismToken != null) {
				TokenQueueManager.insertToken(voteismToken);
			} else {
				TokenQueueManager.insertToken("DUMMY");
			}
		} catch (InterruptedException ex) {
			throw new IOException(ex);
		}
	}
}

