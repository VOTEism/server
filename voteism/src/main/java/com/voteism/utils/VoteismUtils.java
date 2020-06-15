/*
 * Copyright © 2020 AskDesis Inc. or its subsidiaries. All Rights Reserved.
 *
 * This is the confidential unpublished intellectual property of Askdesis
 * Inc, and includes without limitation exclusive copyright and trade
 * secret rights of Askdesis Inc throughout the world.
 */

package com.voteism.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import javax.crypto.KeyGenerator;

import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.common.io.CharStreams;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.NumberParseException.ErrorType;
import com.google.i18n.phonenumbers.PhoneNumberToTimeZonesMapper;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.voteism.aws.secretsmanager.VoteismSecretsManager;
import com.voteism.firestore.FirestoreInitializer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities class for Voteism
 * 
 * @author Ranjit Kollu
 *
 */
@Component
public final class VoteismUtils {
	public static int max_sms_expiry_time = 5;
	public static int max_sms_resend_time = 1;

	public static String supported_country_code = "US";
	public static String supported_country = "United States";
	
	public static String candidate1 = "foo";
	public static String candidate2 = "bar";
	
	public static String candidate1_image = "fooimg"; 
	public static String candidate2_image = "barimg";
	
	public static String candidate1_logo = "foologo"; 
	public static String candidate2_logo = "barlogo";

	public static Map<String, String> stateMap;

	private VoteismUtils() {
	}

	/**
	 * SMS expiry time
	 * 
	 * @param expiryTime
	 */
	@Value("${MAX_SMS_EXPIRY_TIME}")
	public void setMaxSMSExpiryTime(int expiryTime) {
		max_sms_expiry_time = expiryTime;
	}

	/**
	 * Resend time for SMS (one minute)
	 * 
	 * @param resendTime
	 */
	@Value("${MAX_SMS_RESEND_TIME}")
	public void setMaxSMSResendTime(int resendTime) {
		max_sms_resend_time = resendTime;
	}

	/**
	 * Supported country code for voteism
	 * @param countryCode
	 */
	@Value("${SUPPORTED_COUNTRY_CODE}")
	public void setSupportedCountryCode(String countryCode) {
		supported_country_code = countryCode;
	}

	/**
	 * Supported country for voteism
	 * 
	 * @param country
	 */
	@Value("${SUPPORTED_COUNTRY}")
	public void setSupportedCountry(String country) {
		supported_country = country;
	}
	
	/**
	 * First candidate to vote
	 * 
	 * @param cand1
	 */
	@Value("${CANDIDATE_1}")
	public void setCandidate1(String cand1) {
		candidate1 = cand1;
	}
	
	/**
	 * Second candidate to vote
	 * 
	 * @param cand2
	 */
	@Value("${CANDIDATE_2}")
	public void setCandidate2(String cand2) {
		candidate2 = cand2;
	}
	
	/**
	 * Image link for the first candidate
	 * 
	 * @param cand1img
	 */
	@Value("${CANDIDATE_1_IMAGE_URL}")
	public void setCandidate1Img(String cand1img) {
		candidate1_image = cand1img;
	}
	
	/**
	 * Image link for the second candidate
	 * 
	 * @param cand2img
	 */
	@Value("${CANDIDATE_2_IMAGE_URL}")
	public void setCandidate2Img(String cand2img) {
		candidate2_image = cand2img;
	}
	
	/**
	 * Logo for the first candidate
	 * 
	 * @param cand1logo
	 */
	@Value("${CANDIDATE_1_LOGO}")
	public void setCandidate1Logo(String cand1logo) {
		candidate1_logo = cand1logo;
	}
	
	/**
	 * Logo for the second candidate
	 * 
	 * @param cand2logo
	 */
	@Value("${CANDIDATE_2_LOGO}")
	public void setCandidate2Logo(String cand2logo) {
		candidate2_logo = cand2logo;
	}

	/**
	 * Get the randomly generated OTP with minimum length
	 * 
	 * @param timestamp Timestamp seed to generate the OTP
	 * @return generated OTP
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	// Only return OTP that is >= 6 characters
	public static int getOTPwithMinLength(final Instant timestamp)
			throws NoSuchAlgorithmException, InvalidKeyException {
		int retOTP = getOTP(timestamp);

		while (Integer.toString(retOTP).length() < 6) {
			retOTP = getOTP(timestamp);
		}

		return retOTP;
	}

	/**
	 * Method to generated the OTP
	 * 
	 * @param timestamp
	 * @return generated OTP
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public static int getOTP(final Instant timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
		final Duration timeStep = Duration.ofSeconds(30);
		final TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator(timeStep, 6,
				TimeBasedOneTimePasswordGenerator.TOTP_ALGORITHM_HMAC_SHA512);

		final Key key;
		{
			final KeyGenerator keyGenerator = KeyGenerator.getInstance(totp.getAlgorithm());
			// SHA-1 and SHA-256 prefer 64-byte (512-bit) keys; SHA512 prefers 128-byte
			// (1024-bit) keys
			keyGenerator.init(512);
			key = keyGenerator.generateKey();
		}

		return totp.generateOneTimePassword(key, timestamp);
	}

	/**
	 * Get the timezone as a string
	 * 
	 * @return timezone
	 */
	public static String getTimezoneString() {
		final Calendar cal = Calendar.getInstance();
		final long milliDiff = cal.get(Calendar.ZONE_OFFSET);

		// Got local offset, now loop through available timezone id(s).
		final String[] ids = TimeZone.getAvailableIDs();
		String name = null;
		for (String id : ids) {
			TimeZone tz = TimeZone.getTimeZone(id);
			if (tz.getRawOffset() == milliDiff) {
				// Found a match.
				name = id;
				break;
			}
		}

		return name;
	}

	/**
	 * Get timezone
	 * 
	 * @return
	 */
	public static TimeZone getTimezone() {
		final Calendar cal = Calendar.getInstance();
		final long milliDiff = cal.get(Calendar.ZONE_OFFSET);

		// Got local offset, now loop through available timezone id(s).
		final String[] ids = TimeZone.getAvailableIDs();
		TimeZone tz = null;
		for (String id : ids) {
			tz = TimeZone.getTimeZone(id);
			if (tz.getRawOffset() == milliDiff) {
				// Found a match.
				break;
			}
		}

		return tz;
	}

	/**
	 * Convert to Date
	 * 
	 * @param dateToConvert
	 * @return Date
	 */
	public static Date convertToDateViaInstant(final LocalDateTime dateToConvert) {
		return java.util.Date.from(dateToConvert.atZone(ZoneId.of(getTimezoneString())).toInstant());
	}

	/**
	 * Convert to Local date and time from the given date
	 * @param dateToConvert
	 * @return LocalDateTime
	 */
	public static LocalDateTime convertToLocalDateTimeViaInstant(final Date dateToConvert) {
		return LocalDateTime.ofInstant(dateToConvert.toInstant(), ZoneId.of(getTimezoneString()));
	}

	/**
	 * Convert the given time string to GMT time
	 * @param time time string 
	 * @return time in gmt
	 * @throws ParseException
	 */
	public static long timeConversionToGMT(final String time) throws ParseException {
		final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy, HH:mm:ss.SSS a", Locale.ENGLISH); // Specify your
																										// locale

		long unixTime = 0;
		dateFormat.setTimeZone(getTimezone()); // Specify your timezone
		unixTime = dateFormat.parse(time).getTime();
		unixTime = unixTime / 1000;
		return unixTime;
	}

	/**
	 * Convert time in string to long
	 * 
	 * @param time Time to convert
	 * @return time in long
	 * @throws ParseException
	 */
	public static long timeConversion(final String time) throws ParseException {
		final DateFormat dateFormat = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss", Locale.ENGLISH); // Specify your
																										// locale

		long normaltime = 0;
		normaltime = dateFormat.parse(time).getTime();
		return normaltime / 1000;
	}

	/**
	 * Get minute difference with current time
	 * @param d Date
	 * @return
	 */
	public static long getMinuteDifferenceWithCurrentTime(final Date d) {
		final LocalDateTime now = LocalDateTime.now();
		final LocalDateTime postedDate = convertToLocalDateTimeViaInstant(d);
		final Duration duration = Duration.between(postedDate, now);
		final long diffDuration = duration.toMinutes();

		return diffDuration;
	}

	/**
	 * Check if generated OTP expired
	 * 
	 * @param generatedTime Time when the OTP was generated
	 * @return True if expired else False
	 */
	public static boolean OTPExpired(final Date generatedTime) {
		if (getMinuteDifferenceWithCurrentTime(generatedTime) > max_sms_expiry_time) {
			return true;
		}

		return false;
	}

	/**
	 * Check if the OTP needs to be resent
	 * 
	 * @param generatedTime Time when the OTP was generated
	 * @return True if OTP needs to be resent else False
	 */
	public static boolean OTPDonotResend(final Date generatedTime) {
		if (getMinuteDifferenceWithCurrentTime(generatedTime) < max_sms_resend_time) {
			return true;
		}

		return false;
	}

	/**
	 * Get the Timezone for give phone number
	 * 
	 * @param phonenumber User Phone number
	 * @return Get all the timezones for a given phone number
	 * @throws NumberParseException
	 */
	public static List<String> getTimeZoneForPhonenumber(final String phonenumber) throws NumberParseException {
		final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		final PhoneNumber phone = phoneUtil.parse(phonenumber, null);
		final PhoneNumberToTimeZonesMapper timeZonesMapper = PhoneNumberToTimeZonesMapper.getInstance();

		return timeZonesMapper.getTimeZonesForNumber(phone);
	}

	/**
	 * Check if the give phone number is a valid number or not
	 * 
	 * @param phoneNumber user phone number
	 * @param verifyCountryCode country code
	 * @return True if valid, else False
	 * @throws NumberParseException
	 */
	public static boolean isValidPhoneNumber(final String phoneNumber, final boolean verifyCountryCode)
			throws NumberParseException {
		final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		final PhoneNumber phone = phoneUtil.parse(phoneNumber, null);
		final PhoneNumberOfflineGeocoder offlineGeocoder = PhoneNumberOfflineGeocoder.getInstance();
		final String description = offlineGeocoder.getDescriptionForNumber(phone, Locale.ENGLISH, supported_country);

		if (verifyCountryCode && (phone.getCountryCode() != phoneUtil.getCountryCodeForRegion(supported_country))
				&& (!description.equalsIgnoreCase(supported_country))) {
			throw new NumberParseException(ErrorType.INVALID_COUNTRY_CODE,
					"This app is not supported in your country!");
		}

		return phoneUtil.isValidNumber(phone);

	}

	/**
	 * Check if the given phone number is mobile number of not
	 * 
	 * @param phoneNumber user phone number
	 * @return True if mobile, else False
	 * @throws NumberParseException
	 */
	public static boolean isMobile(final String phoneNumber) throws NumberParseException {
		final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		final PhoneNumber phone = phoneUtil.parse(phoneNumber, null);
		final PhoneNumberType phoneNumberType = phoneUtil.getNumberType(phone);

		if (!phoneNumberType.name().equalsIgnoreCase(phoneNumberType.MOBILE.name())) {
			Twilio.init(VoteismSecretsManager.getTwilioAccountId(), VoteismSecretsManager.getTwilioAccountKey());
			com.twilio.rest.lookups.v1.PhoneNumber twiPhoneNumber = com.twilio.rest.lookups.v1.PhoneNumber
					.fetcher(new com.twilio.type.PhoneNumber(phoneNumber)).setType(Arrays.asList("carrier")).fetch();
			return twiPhoneNumber.getCarrier().get("type")
					.equalsIgnoreCase(com.twilio.rest.lookups.v1.PhoneNumber.Type.MOBILE.name());
		}

		return false;
	}

	/**
	 * Get the phone number and ISD code as a tuple
	 * 
	 * @param phoneNumber user phone number
	 * @return tuple containing the phone number and it's ISD code
	 */
	public static Tuple2<String, String> getPhonenumberAndISDCode(final String phoneNumber) {
		final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		final String countryCode = Integer.toString(phoneUtil.getCountryCodeForRegion(supported_country_code));
		final String isdCode = "+".concat(countryCode);
		final String[] actualPhonenumbers = StringUtils.splitByWholeSeparator(phoneNumber, isdCode);

		return Tuple.tuple(isdCode, actualPhonenumbers[0]);
	}

	/**
	 * Get the phone number concatenated with the ISD code
	 * 
	 * @param phoneNumber user phone number
	 * @return ISD code concatenated with the phone number (Example : +12356789000)
	 */
	public static String getPhonenumbeWithISDCode(final String phoneNumber) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		String countryCode = Integer.toString(phoneUtil.getCountryCodeForRegion(supported_country_code));
		String isdCode = "+".concat(countryCode);
		return isdCode.concat(phoneNumber);
	}

	/**
	 * Load the resource using the class loader
	 * 
	 * @param path Path to the resource
	 * @return Resource as string
	 */
	public static String loadResource(String path) {
		final InputStream stream = VoteismUtils.class.getClassLoader().getResourceAsStream(path);
		checkNotNull(stream, "Failed to load resource: %s", path);
		try (InputStreamReader reader = new InputStreamReader(stream)) {
			return CharStreams.toString(reader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Load all the statenames for the supported country in the app
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void loadStatenames() throws InterruptedException, ExecutionException {
		if (null == stateMap || stateMap.size() == 0) {
			final Firestore firestore = FirestoreInitializer.getFirestoreInstance();
			final CollectionReference collectionRef = firestore.collection("country").document(supported_country)
					.collection("states");

			final ApiFuture<QuerySnapshot> future = collectionRef.get();
			final QuerySnapshot querySnapshot = future.get();

			stateMap = new HashMap<String, String>();
			for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
				Map<String, Object> data = doc.getData();
				for (Map.Entry<String, Object> entry : data.entrySet()) {
					stateMap.put(doc.getId(), (String) entry.getValue());
				}
			}
		}
	}

	/**
	 * Get the state's fullnames
	 * 
	 * @return All states with their full names (Example CA : California)
	 */
	private static List<String> getStateFullnames() {
		List<String> stateFullnames = null;

		if (null != stateMap && stateMap.size() > 0) {
			stateFullnames = new ArrayList<String>();
			stateFullnames.addAll(stateMap.values());
		}

		return stateFullnames;
	}

	/**
	 * Get the fullname for a state
	 * 
	 * @param shortname State short name
	 * @return fullname for the state
	 */
	public static String getStateFullname(final String shortname) {
		final List<String> stateFullnames = getStateFullnames();
		String fullStatename = "";

		if (stateFullnames != null && stateFullnames.contains(shortname)) {
			fullStatename = shortname;
		}

		else if (null != stateMap && stateMap.size() > 0) {
			fullStatename = stateMap.get(shortname);
		}

		return fullStatename;
	}
	
	/**
	 * Get the name, image and log for the first candidate
	 * 
	 * @return Map containing the first candidate info
	 */
	private static Map<String, String> getCannedCandidate1Info() {
		final Map<String, String> candidateMap = new HashMap<String, String>();
		
		candidateMap.put("name", candidate1);
		candidateMap.put("image", candidate1_image);
		candidateMap.put("logo", candidate1_logo);
		
		return candidateMap;
	}
	
	/**
	 * Get the name, image and log for the second candidate
	 * 
	 * @return Map containing the second candidate info
	 */
	private static Map<String, String> getCannedCandidate2Info() {
		final Map<String, String> candidateMap = new HashMap<String, String>();
		
		candidateMap.put("name", candidate2);
		candidateMap.put("image", candidate2_image);
		candidateMap.put("logo", candidate2_logo);
		
		return candidateMap;
	}
	
	/**
	 * Get the name, image and log for both the candidates
	 * 
	 * @return Map containing both the candidates info
	 */
	public static List<Map<String, String>> getCannedCandidateInfo() {
		final List<Map<String, String>> candidatesList = new ArrayList<Map<String, String>>();
		
		candidatesList.add(getCannedCandidate1Info());
		candidatesList.add(getCannedCandidate2Info());
		
		return candidatesList;
	}
}
