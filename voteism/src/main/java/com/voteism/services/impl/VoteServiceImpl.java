/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.services.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Component;

import com.google.api.core.ApiFuture;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.BigQueryError;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;

import com.voteism.bigquery.BigQueryInitializer;
import com.voteism.domain.UserVote;
import com.voteism.exceptions.VoteException;
import com.voteism.firestore.FirestoreInitializer;
import com.voteism.services.VoteService;
import com.voteism.ses.SendWithSes;
import com.voteism.utils.VoteismUtils;

/**
 * Implementation class for Phone operations
 * 
 * @author Ranjit Kollu
 *
 */
@Component
public class VoteServiceImpl implements VoteService {
	public void saveUserVote(final UserVote userVote)
			throws ParseException, InterruptedException, VoteException, ExecutionException {		
		final Map<String, Object> row = getRowToInsert(userVote);
		saveRecordToBigQuery(row);
		saveRowToFireStore(row);
	}

	private Map<String, Boolean> initializeAndGetResultMap() {
		final Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
		resultMap.put("BigQuery", false);
		resultMap.put("FireStore", false);

		return resultMap;
	}

	private Map<String, Object> getRowToInsert(final UserVote userVote) throws ParseException {
		final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		final LocalDateTime now = LocalDateTime.now();
		final Date date = formatter.parse(dtf.format(now));
		
		final Map<String, Object> row = new HashMap<>();
		row.put("uid", VoteismUtils.getPhonenumbeWithISDCode(userVote.getUser().getPhonenumber()));
		row.put("day", formatter.format(date));
		row.put("vote", userVote.getEncryptedVote());
		row.put("signature", userVote.getVoteSignature());
		row.put("is_simulator", userVote.getUser().getUserDeviceDetails().isSimulator());
		row.put("os", userVote.getUser().getUserDeviceDetails().getDeviceOS());
		row.put("device_id", userVote.getUser().getUserDeviceDetails().getDeviceToken());
		row.put("ip", userVote.getUser().getUserDeviceDetails().getIPAddress());
		row.put("country", userVote.getUser().getLocation().getCountry());
		row.put("state", VoteismUtils.getStateFullname(userVote.getUser().getLocation().getState()));
		row.put("city", userVote.getUser().getLocation().getCity());
		row.put("latitude", userVote.getUser().getLocation().getCurrentLatitude());
		row.put("longtitude", userVote.getUser().getLocation().getCurrentLongitude());
		row.put("mac_address", userVote.getUser().getUserDeviceDetails().getMACAddress());
		row.put("inserted_at", VoteismUtils.timeConversion(userVote.getGMTtime()));
		row.put("timezone", userVote.getTimezone());
		row.put("voted_time", userVote.getVotedTime());
		
		return row;
	}

	private void saveRecordToBigQuery(final Map<String, Object> row) throws VoteException, InterruptedException {
		final BigQuery bigQuery = BigQueryInitializer.getBigQueryInstance();
		final String datasetId = "development";
		final TableId tableId = TableId.of(datasetId, "votes");

		// Create an insert request
		final InsertAllRequest insertRequest = InsertAllRequest.newBuilder(tableId).addRow(row).build();
		final InsertAllResponse insertResponse = bigQuery.insertAll(insertRequest);

		if (insertResponse.hasErrors()) {
			final StringBuilder errorBuilder = new StringBuilder();

			for (Map.Entry<Long, List<BigQueryError>> entry : insertResponse.getInsertErrors().entrySet()) {
				for (BigQueryError error : entry.getValue()) {
					errorBuilder.append(error.getMessage());
					errorBuilder.append("\n");
				}
			}

			throw new VoteException(errorBuilder.toString());
		}

		// Create a query request
		final QueryJobConfiguration queryConfig = QueryJobConfiguration
				.newBuilder("SELECT * FROM development.votes where day is NULL").build();
		// Read rows
		for (FieldValueList fieldRow : bigQuery.query(queryConfig).iterateAll()) {
			System.out.println(fieldRow);
		}
	}

	private void saveRowToFireStore(final Map<String, Object> row) throws ExecutionException, InterruptedException {
		final Firestore firestore = FirestoreInitializer.getFirestoreInstance();
		final DocumentReference docRef = firestore.collection("votes").document((String)row.get("country"))
				.collection((String) row.get("day")).document((String) row.get("uid"));

		final ApiFuture<WriteResult> futureWrite = docRef.set(row);
		futureWrite.get();
		
		DocumentReference userDocRef = firestore.collection("users").document((String)row.get("uid"));

		final ApiFuture<DocumentSnapshot> userFuture = userDocRef.get();
		final DocumentSnapshot document = userFuture.get();


		final String deviceId = document.getString("device_id");
		final String deviceOS = document.getString("device_os");
		final boolean isSimulator = document.getBoolean("is_simulator");
		final String macaddress = document.getString("macaddress");
		final String ipAddress = document.getString("ipAddress");
		final boolean loginStatus = document.getBoolean("loginStatus");
		final Date lastlogintime = document.getDate("lastlogintime");
		
		final Map<String, Object> docData = new HashMap<>();
		docData.put("lastlogintime", lastlogintime);
		docData.put("city", (String)row.get("city"));
		docData.put("state", (String)row.get("state"));
		docData.put("country", (String)row.get("country"));

		docData.put("current_latitude", (String)row.get("latitude"));
		docData.put("current_longitude", (String)row.get("longtitude"));	
		docData.put("device_id", deviceId);
		docData.put("device_os", deviceOS);
		docData.put("is_simulator", isSimulator);
		docData.put("macaddress", macaddress);
		docData.put("ipAddress", ipAddress);
		docData.put("lastlogintime", lastlogintime);
		docData.put("loginStatus", loginStatus);
		
		final ApiFuture<WriteResult> userFutureWrite = userDocRef.set(docData);
		userFutureWrite.get();
		SendWithSes.updateUser((String)row.get("uid"), (String)row.get("country"), (String)row.get("city"));
	}
}
