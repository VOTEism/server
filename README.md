# VOTEism - The Political Opinion Poll App.
* This repository contains the server side code for VOTEism.
* The following is an overview of the end-to-end steps involved.

## 1. PROCESS OVERVIEW:
### 1.1 Registration
* [CLIENT] User enters mobile number to start registration process.
* [SERVER] Twilio carrier lookup is done to identify valid USA mobile numbers.
* [SERVER] OTP is sent to valid numbers via sendwithses.com or twilio.com (fallback).
* [CLIENT] User enters OTP.
* [SERVER] OTP entered is verified and valid user is registered.

### 1.2 Voting
* [CLIENT] User selects and confirms electoral candidate.
* [SERVER] Server sends the public key (4096 bit) to user.
* [CLIENT] The vote is encrypted using the public key along with few other details like device id, location, ip address, etc. The encrypted vote is signed and both the encrypted vote and the signature are sent to the server.
* [SERVER] Server receives the encrypted vote, signature, location, device id, etc. This information is stored in BigQuery and in Firestore.
* [SERVER] Success/Failure message is sent to client.

### 1.3 Counting
* [COMPUTER-1] Encrypted votes are downloaded to computer-1.
* [COMPUTER-2 (Air-Gapped)] Encrypted votes are transferred to computer-2.
* [COMPUTER-2] The signature of each vote is verified and the encrypted vote is decrypted with a password protected private key.
* [COMPUTER-2] Results are aggregated after removing duplicates (only one vote counted per mobile-number/device-id combination).
* [COMPUTER-1] Aggregated results transferred to computer-1 and uploaded to database.

## 2. MINIMAL REQUIREMENTS
* Java 8
* Springboot
* Google cloud account - Server side writes to Firestore db and Bigquery
* AWS account - AWS lambda (Optional), AWS API Gateway, AWS Secrets Manager (Mandatory)
* Twilio account - For verifying whether the registering phone is a mobile or not
* SendWithSes account - Register with SendWithSes application to send SMS via REST API calls to sendwithses
* Maven 3 - To build the project

## 3. BUILD
From the top level directory run "mvn clean package" to build the project
The jar file is stored under the target folder of the project

## 4. What important headers get returned in HTTP response as part of successful OTP verification/login ?
* VOTEISM_TOKEN - API ACCESS KEY (x-api-key) for the AWS API Gateway, which should be passed in as part of other REST requests.
* VOTEISM_ACCESS_TOKEN - JWT token encapsulating the User phone number which gets as part of other REST requests to recognize the user sending the request.
* VOTEISM_FIRESTORE_TOKEN - Firestore token used by the client application to access the firestore database.

## 5. OTP Status Codes
Once the user tries to register with the application, server sends a OTP to the client. Client needs to verify the OTP with the server. Different OTP status codes -

* NOT_VERIFIED - If the OTP is not verified
* OK - If the OTP has been successfully verified
* FAIL - If the OTP verification fails
* EXPIRED - If the OTP expires

## 6. Rate Throttling
User cannot vote more than 3 times per minute.

## 7. AWS Secrets Manager
Voteism secrets are stored in the AWS Secrets Manager. Following secrets are stored -

* TWILIO ACCOUNT ID
* TWILIO AUTH TOKEN
* SEND WITH SES KEY to send SMS
* X_API_KEY (AWS API Gateway key)
* JWT SECRET to sign the JWT token for VOTEISM_ACESS_TOKEN using Auth0
* JWT EXPIRATION - Time validity of VOTEISM_ACCESS_TOKEN
* FIREBASE SECRET - Firebase account secret
* VOTEISM PUBLIC KEY - Public key used to encrypt the user vote data

## 8. REST API Requests
#### 8.1 Register User
###### REST API end point - /voteism/users/login
###### Request Type - POST
###### Description - Sends a OTP to the client application that needs to be verified for successful registration
###### Example Request Body -
<pre><code>
{
  "phonenumber":"+12345678900",
  "location": {
  	"city" : "Seattle",
  	"state": "WA",
  	"country": "United States",
  	"current_latitude" : "42.09032234895876",
  	"current_longitude" : "-71.34104757167215"
  },
  "userDeviceDetails" :{
  	"deviceToken" : "570b81b2-289f-4558-vote-70a1f86ca690",
  	"deviceOS" : "Android",
  	"isSimulator" : false,
  	"macAddress" : "02:00:00:00:00:00",
  	"ipAddress" : "192.168.0.113"
  }
}
</code></pre>

#### Response Body
<pre><code>
{
    "otpstatus": "NOT_VERIFIED"
}
</code></pre>

#### 8.2 Verify OTP
###### REST API end point - /voteism/otp
###### Request Type - POST
###### Description - Verifies the OTP
###### Example Request Body -
<pre><code>
{
	"otp" : "744873",
	"operation" : "verify",
	"user" : {
	  "phonenumber":"+12345678900",
	  "location": {
	  	"city" : "Seattle",
	  	"state": "WA",
	  	"country": "United States",
	  	"current_latitude" : "42.09032234895876",
	  	"current_longitude" : "-71.34107757167215"
	  },
	  "userDeviceDetails" :{
	  	"deviceToken" : "570b81b2-289f-4558-vote-70a1f86ca690",
	  	"deviceOS" : "Android",
	  	"simulator" : false,
	  	"macAddress" : "02:00:00:00:00:00",
	  	"ipAddress" : "192.168.0.113"
	  }
	}
}
</code></pre>

### Response Body
<pre><code>
{
    "otpstatus": "OK",
    candidates": [
        {
            "image": "https://firebasestorage.googleapis.com/voteism.appspot.com/o/candidates%2Ftrump.jpg",
            "name": "Donald Trump",
            "logo": "https://firebasestorage.googleapis.com/voteism.appspot.com/o/candidates%2FtrumpLogo.png"
        },
        {
            "image": "https://firebasestorage.googleapis.com/voteism.appspot.com/o/candidates%2Fbiden.jpg",
            "name": "Joe Biden",
            "logo": "https://firebasestorage.googleapis.com/voteism.appspot.com/o/candidates%2FbidenLogo.png"
        }
    ],
    "user": {
        "phonenumber": "+12345678900",
        "loginStatus": true,
        "location": {
            "city": "Seattle",
            "state": "WA",
            "country": "United States",
            "currentLongitude": "-61.34104757167255",
            "currentLatitude": "42.09032234895876"
        },
        "userDeviceDetails": {
            "deviceToken": "570b81b2-289f-4558-vote-70a1f86ca690",
            "deviceOS": "Android",
            "simulator": false,
            "ipAddress": "192.168.0.113",
            "macaddress": "02:00:00:00:00:00"
        },
        "lastLogintime": "2020-05-09T00:21:13.922+0000"
    }
}
</code></pre>

#### 8.3 User Login (After Registration)
###### REST API end point - /voteism/users/login
###### Request Type - POST
###### Description - User logs in to the app any time after successful registration
###### Example Request Body -
<pre><code>
{
  "phonenumber":"+12345678900",
  "location": {
  	"city" : "Seattle",
  	"state": "WA",
  	"country": "United States",
  	"current_latitude" : "42.09032234895876",
  	"current_longitude" : "-71.34104757167215"
  },
  "userDeviceDetails" :{
  	"deviceToken" : "570b81b2-289f-4558-vote-70a1f86ca690",
  	"deviceOS" : "Android",
  	"isSimulator" : false,
  	"macAddress" : "02:00:00:00:00:00",
  	"ipAddress" : "192.168.0.113"
  }
}
</code></pre>

#### Response Body
<pre><code>
{
    candidates": [
        {
            "image": "https://firebasestorage.googleapis.com/voteism.appspot.com/o/candidates%2Ftrump.jpg",
            "name": "Donald Trump",
            "logo": "https://firebasestorage.googleapis.com/voteism.appspot.com/o/candidates%2FtrumpLogo.png"
        },
        {
            "image": "https://firebasestorage.googleapis.com/voteism.appspot.com/o/candidates%2Fbiden.jpg",
            "name": "Joe Biden",
            "logo": "https://firebasestorage.googleapis.com/voteism.appspot.com/o/candidates%2FbidenLogo.png"
        }
    ],
    "user": {
        "phonenumber": "+12345678900",
        "loginStatus": true,
        "location": {
            "city": "Seattle",
            "state": "WA",
            "country": "United States",
            "currentLongitude": "-71.34104757167215",
            "currentLatitude": "42.09032234895876"
        },
        "userDeviceDetails": {
            "deviceToken": "570b81b2-289f-4558-vote-70a1f86ca690",
            "deviceOS": "Android",
            "simulator": false,
            "ipAddress": "192.168.0.113",
            "macaddress": "02:00:00:00:00:00"
        },
        "lastLogintime": "2020-05-09T00:21:13.922+0000"
    }
}
</code></pre>

#### 8.4 User Logout
###### REST API end point - /voteism/users/logout
###### Request Type - POST
###### Description - User logs out of the application
###### Required HTTP Headers for the request - VOTEISM_TOKEN, VOTEISM_ACCESS_TOKEN
###### Response Body
<pre><code>
User has successfully logged out.
</code></pre>

#### 8.5 Resend OTP
###### REST API end point - /voteism/otp
###### Request Type - POST
###### Description - User requests the client application to resend the OTP
###### Example Request Body
<code><pre>
{
	"phonenumber" : "+12345678900",
	"operation" : "resend"
}
</code></pre>

#### Response Body
<pre><code>
{
    "otpstatus": "NOT_VERIFIED"
}
</code></pre>

#### 8.6 Refresh Firestore Token
###### REST API end point - /voteism/users/refresh/firestoretoken
###### Request Type - POST
###### Description - Refresh the token to access the firestore database (because the firestore token expires periodically)
###### Required HTTP Headers for the request - VOTEISM_TOKEN, VOTEISM_ACCESS_TOKEN
###### Response Body
<pre><code>
{
    "firestoretoken": "eyJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJodHRwczovL2lkZW50aXR5dG9vbGtpdC5nb29nbGVhcGlzLmNvbS9nb29nbGUuaWRlbnRpdHkuaWRlbnRpdHl0b29sa2l0LnYxLklkZW50aXR5VG9vbGtpdCIsImNsYWltcyI6eyJwcmVtaXVtQWNjb3VudCI6dHJ1ZX0sImV4cCI6MTU4ODczNDEyNCwiaWF0IjoxNTg4NzMwNTI0LCJpc3MiOiJicS1maXJlc3RvcmUtZGZ2QHZvdGVpc20uaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20iLCJzdWIiOiJicS1maXJlc3RvcmUtZGV2QHZvdGVpc20uaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20iLCJ1aWQiOiJ1c2VyIn0.DrBNdr3IDVOTEpJhrcFbvvP5c-0ZPW-CnVQLnbK7GclZD_ht0U-PO7PNKQbb5e0kl5xZ0LzEhYJfveIBRJNV3rpO3b9sGjrDCkZHM0bscZoEARuFjGgCnQfLydSAxfT0UBXKXPjk25oGGuDc2Re_wEe7oHVSPX7gW_LYtVICqLab0DDGzRFelQ1oQOqOarXlpmPIXvJAxqdnnRByQMzGPYfa4rmGpCGdAm2dhSaSBLUkZZ3N2qLp1SzuCCg3qnVLxEbbx3cPvotetojg54sRryb4SGr7jtZ8oS03HOi_IAIef8wz9e-CjdlHUXk"
}
</code></pre>

#### 8.7 Fetch Public Key
###### REST API end point - /voteism/fetch/publickey
###### Request Type - GET
###### Description - Fetch the public key to be used to encrypt the user vote data by the client application
###### Required HTTP Headers for the request - VOTEISM_TOKEN, VOTEISM_ACCESS_TOKEN
###### Response Body
<pre><code>
{
    "publickey": "-----BEGIN PUBLIC KEY----- MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAsYgpkPThsOWKJSpSIVWl ZOxUBY74j+W+OyfDWEpKyQ+/jocRSAujSetoXNLnUwFMWIQX94J1GFu9QVKcy/e1 vvojp2lfViU+0dudl6hgteRnD3QVlBsrrT1NGs9QzSUezM7gDIXcFk9TawdvTAU6 nI4suObUcbRJT8oiUwQSV1SllxiGCEKVscr/VhLh9iKhr+Cj22Dr3pV3eatooECf pTY4ZR7XGJmkcris2RYCnwpq3aTSKbQdY+U44Vy57A7qs7c5cWlFkFLGVdLjMUZR 7BQydkBlWDVh2MfpFJX8W7zQUh5TjgKH7SRVOTElLlUCF4YWW3tvotepSTY1s+5s 6M+h/AchQWbLsJnFzG9AbihwIe1LOwWnPr7WM5HR1dGqJXJNq/vVHPu/y7Fb6oDC W3p4j7tBVXtX7E4/btg3MccHtHHNxnvb0rGBAdwOi7OkgRUXX6ZKIARYuFnN2xuz yCOeFth00XO0TyT/QT9HuMMFF1BTC7nz1nax6fGmEuQjt/woLWZ45J0jXJEFbXGt lQgvCoN/7nRn2Csiypm+mxZKs2izBejQDLdkz8D/ZPnf8Jf1oBrFZbGxbB5uKxWo 4Tigu+MXogjumPmyBFfVJcnN/Mdm+O2EBEhgHEWPmf0A1UZvbpK11ou74QXk5pKt==-----END PUBLIC KEY----- "
}
</code></pre>

#### 8.8 Save User Vote
###### REST API end point - /voteism/users/vote
###### Request Type - POST
###### Description - Save the encrypted user vote to firestore and bigquery
###### Required HTTP Headers for the request - VOTEISM_TOKEN, VOTEISM_ACCESS_TOKEN
###### Example Request Body -
<pre><code>
{
	"encryptedVote" : "WR+fxqNbpup4VhLRNbLKdw0GKRRQcmzLbZmc5cmIYRiR8tVeVOV2gegPYNZOw+KhemFkMnCS1UhWNM7pwqfV0wkJqmRzOFDPzAvx8jKWiFmfkhQFpkYTRH/+XezMaY6qKwee+k4Fn9QJCVVoju3xQhxmPbpmn+wr6zRqAQNEaj8hPasZSKk81jtqWzNnlty4FqpHWU/CeUfGkf71YQNDZv2wjD/yK0ClHImwCA2fs7Tfo+VE+v7zvoEK57lEUGOAEIPW4fayXF7/B29OJf2bTUZQgaVQwTok7w+e8E4MzT+TklbD/7N85o4UfP7XP6XsrhLIAklLCClmAxFch9TAPPioJGGLoIQqCj26RTD873jVv7SjD+BOICDbgOTyT4TJKa0gdOd2yFB3ex/b0/vndaCgGrSjFMIMYYRMeXEMPiENmg0/how9BPZ8+hmoQgO46QcLTiQpHEEldm063rFfkf9k5PC7YkMOA/M48hA9r8Nkpgl9AC4ko1yatxkxLcKxti/bYZgnjH7SJ26RtEdJAYsa6ukw5jc463KYd2FSFyi9r7TEmyxUtnE0/ikuC/vfDLcXVOTErQq/BdSM3rQjqraBcWIYUt4n+J0901ES2DT+CvM2AhybRpv6tr3FGwh6M6TsQK7K60WncNQUH+cQYquH+i8+9jIZUMH3BN++hgU=",
	"voteSignature" : "c5b6c9eece7872a7462f2e000vote52c973b7444a7775a2308f0f83b4896e82f",
	"user" : {
		  "phonenumber":"+12345678900",
		  "location": {
		  	"city" : "Seattle",
		  	"state": "WA",
		  	"country": "United States",
		  	"current_latitude" : "42.09232234895876",
		  	"current_longitude" : "-71.94104757167215"
		  },
		  "userDeviceDetails" :{
		  	"deviceToken" : "570b92b2-289f-4558-vote-70a1ismca690",
		  	"deviceOS" : "Android",
		  	"simulator" : false,
		  	"macAddress" : "02:00:00:00:00:00",
		  	"ipAddress" : "192.168.0.113"
		  }
		},
		"timezone" : "-5:00", //Timezone
		"votedTime": "Sat 25 Apr 2020 22:41:01 GMT +530", //Local time
		"gmtTime": "Sat 25 Apr 2020 06:36:22" //GMT time
}
</code></pre>

#### Response Body
<pre><code>
“You have cast your vote successfully”
</code></pre>
