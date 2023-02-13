/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mdmi.rt.service.web;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// [START healthcare_create_resource]
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.gax.paging.Page;
import com.google.api.services.healthcare.v1.CloudHealthcare;
import com.google.api.services.healthcare.v1.CloudHealthcareScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;

public class FhirResourceCreate {

	private static Logger logger = LoggerFactory.getLogger(FhirResourceCreate.class);

	public static final String FHIR_NAME = "projects/%s/locations/%s/datasets/%s/fhirStores/%s";

	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	public static int TIMEOUT_MILLIS = 5;

	public static String postBundle(String jsonPath, String fhirStoreName, String bundleContent)
			throws URISyntaxException, IOException {

		GoogleCredentials credential = GoogleCredentials.fromStream(new FileInputStream(jsonPath)).createScoped(
			Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));
		HttpRequestInitializer requestInitializer = request -> {
			new HttpCredentialsAdapter(credential).initialize(request);
			request.setConnectTimeout(1); // 1 minute connect timeout
			request.setReadTimeout(1); // 1 minute read timeout
			// request.set

			// request.setSocketTimeout(TIMEOUT_MILLIS)
			// request.setConnectTimeout(TIMEOUT_MILLIS)
			// request.setConnectionRequestTimeout(TIMEOUT_MILLIS)
		};

		// Build the client for interacting with the service.
		CloudHealthcare client = new CloudHealthcare.Builder(
			HTTP_TRANSPORT, JSON_FACTORY, requestInitializer).setApplicationName("mdmi-transform-postbundle").build();

		HttpClient httpClient = HttpClients.createDefault();

		String uri = String.format("%sv1/%s/fhir", client.getRootUrl(), fhirStoreName);
		URIBuilder uriBuilder = new URIBuilder(uri).setParameter(
			"access_token", credential.refreshAccessToken().getTokenValue());
		StringEntity requestEntity = new StringEntity(bundleContent);

		logger.info("Post Bundle: " + uri);

		HttpUriRequest request = RequestBuilder.post().setUri(uriBuilder.build()).setEntity(requestEntity).addHeader(
			"Content-Type", "application/fhir+json").addHeader("Accept-Charset", "utf-8").addHeader(
				"Accept", "application/fhir+json; charset=utf-8").build();

		// Execute the request and process the results.
		logger.info("Post Bundle: execute ");
		HttpResponse response = null;
		int loopctr = 0;
		while (response == null && loopctr < 10) {
			try {
				loopctr++;

				response = httpClient.execute(request);
			} catch (ClientProtocolException e) {
				// This is a different error then timeout
				throw e;

			} catch (IOException e) {
				logger.error(e.getLocalizedMessage());
			}
		}

		logger.info("Post Bundle: response ");
		HttpEntity responseEntity = response.getEntity();
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			logger.error(String.format("Exception creating FHIR resource: %s\n", response.getStatusLine().toString()));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			responseEntity.writeTo(baos);
			logger.error(baos.toString());
			throw new RuntimeException(baos.toString());
		}

		String responseString = EntityUtils.toString(responseEntity, "UTF-8");
		return responseString;
	}

	public static String query(String jsonPath, String fhirStoreName, String query)
			throws IOException, URISyntaxException {

		GoogleCredentials credential = GoogleCredentials.fromStream(new FileInputStream(jsonPath)).createScoped(
			Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));
		HttpRequestInitializer requestInitializer = request -> {
			new HttpCredentialsAdapter(credential).initialize(request);
			request.setConnectTimeout(60000); // 1 minute connect timeout
			request.setReadTimeout(60000); // 1 minute read timeout
		};

		// Build the client for interacting with the service.
		CloudHealthcare client = new CloudHealthcare.Builder(
			HTTP_TRANSPORT, JSON_FACTORY, requestInitializer).setApplicationName("mdmi-transform-postbundle").build();

		HttpClient httpClient = HttpClients.createDefault();

		String uri = String.format("%sv1/%s/fhir/%s", client.getRootUrl(), fhirStoreName, query);
		URIBuilder uriBuilder = new URIBuilder(uri).setParameter(
			"access_token", credential.refreshAccessToken().getTokenValue());

		HttpUriRequest request = RequestBuilder.get().setUri(uriBuilder.build()).addHeader(
			"Content-Type", "application/fhir+json").addHeader("Accept-Charset", "utf-8").addHeader(
				"Accept", "application/fhir+json; charset=utf-8").build();

		// Execute the request and process the results.
		HttpResponse response = httpClient.execute(request);
		HttpEntity responseEntity = response.getEntity();
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			logger.error(String.format("Exception creating FHIR resource: %s\n", response.getStatusLine().toString()));
			responseEntity.writeTo(System.err);
			throw new RuntimeException();
		}
		logger.trace("Query Result: " + query);

		String responseString = EntityUtils.toString(responseEntity, "UTF-8");

		JSONParser parser = new JSONParser();

		try {
			Object obj = parser.parse(responseString);
			JSONObject jsonObject = (JSONObject) obj;
			if (jsonObject.containsKey("entry")) {
				JSONArray entries = (JSONArray) jsonObject.get("entry");
				JSONObject entry = (JSONObject) entries.get(0);
				JSONObject resource = (JSONObject) entry.get("resource");

				return (String) resource.get("id");
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void authExplicit(String jsonPath) throws IOException {
		// You can specify a credential file by providing a path to GoogleCredentials.
		// Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath)).createScoped(
			Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
		Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

		logger.trace("Buckets:");
		Page<Bucket> buckets = storage.list();
		for (Bucket bucket : buckets.iterateAll()) {
			System.out.println(bucket.toString());
		}
	}

	private static CloudHealthcare createClient(String jsonPath) throws IOException {

		GoogleCredentials credential = GoogleCredentials.fromStream(new FileInputStream(jsonPath)).createScoped(
			Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));
		// Create a HttpRequestInitializer, which will provide a baseline configuration to all requests.
		HttpRequestInitializer requestInitializer = request -> {
			new HttpCredentialsAdapter(credential).initialize(request);
			request.setConnectTimeout(60000); // 1 minute connect timeout
			request.setReadTimeout(60000); // 1 minute read timeout
		};

		// Build the client for interacting with the service.
		return new CloudHealthcare.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer).setApplicationName(
			"your-application-name").build();
	}

	// projects/zanenet-njinck/locations/us-central1/datasets/dev-zanenet-njinck/fhirStores/dev-mdix-datastore
	// projects/zanenet-njinck/locations/us-central1/datasets/dev-zanenet-njinck/fhirStores/dev-mdix-datastore

	public static void fhirResourceDelete(String jsonPath, String fhirStoreName, String resourceName)
			throws IOException, URISyntaxException {
		GoogleCredentials credential = GoogleCredentials.fromStream(new FileInputStream(jsonPath)).createScoped(
			Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));
		HttpRequestInitializer requestInitializer = request -> {
			new HttpCredentialsAdapter(credential).initialize(request);
			request.setConnectTimeout(60000); // 1 minute connect timeout
			request.setReadTimeout(60000); // 1 minute read timeout
		};

		// Build the client for interacting with the service.
		CloudHealthcare client = new CloudHealthcare.Builder(
			HTTP_TRANSPORT, JSON_FACTORY, requestInitializer).setApplicationName("mdmi-transform-postbundle").build();

		HttpClient httpClient = HttpClients.createDefault();
		String uri = String.format("%sv1/%s", client.getRootUrl(), resourceName);
		URIBuilder uriBuilder = new URIBuilder(uri).setParameter(
			"access_token", credential.refreshAccessToken().getTokenValue());

		HttpUriRequest request = RequestBuilder.delete().setUri(uriBuilder.build()).addHeader(
			"Content-Type", "application/fhir+json").addHeader("Accept-Charset", "utf-8").addHeader(
				"Accept", "application/fhir+json; charset=utf-8").build();

		// Execute the request and process the results.
		// Regardless of whether the operation succeeds or
		// fails, the server returns a 200 OK HTTP status code. To check that the
		// resource was successfully deleted, search for or get the resource and
		// see if it exists.
		HttpResponse response = httpClient.execute(request);
		HttpEntity responseEntity = response.getEntity();
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			String errorMessage = String.format(
				"Exception deleting FHIR resource: %s\n", response.getStatusLine().toString());
			// System.err.print(errorMessage);
			// responseEntity.writeTo(System.err);
			throw new RuntimeException(errorMessage);
		}
		System.out.println("FHIR resource deleted.");
		responseEntity.writeTo(System.out);
	}

}
