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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

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
	public static final String FHIR_NAME = "projects/%s/locations/%s/datasets/%s/fhirStores/%s";

	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	public static void fhirResourceCreate(String jsonPath, String fhirStoreName, String resourceType)
			throws IOException, URISyntaxException {
		// String fhirStoreName =
		// String.format(
		// FHIR_NAME, "your-project-id", "your-region-id", "your-dataset-id", "your-fhir-id");
		// String resourceType = "Patient";

		// projects/mystic-sun-330921/locations/LOCATION/datasets/DATASET_ID/fhirStores/FHIR_STORE_ID/fhir/Patient/PATIENT_

		// Initialize the client, which will be used to interact with the service.
		CloudHealthcare client = createClient(jsonPath);
		HttpClient httpClient = HttpClients.createDefault();
		String uri = String.format("%sv1/%s/fhir/%s", client.getRootUrl(), fhirStoreName, resourceType);
		URIBuilder uriBuilder = new URIBuilder(uri).setParameter("access_token", getAccessToken());
		StringEntity requestEntity = new StringEntity(
			"{\"resourceType\": \"" + resourceType + "\", \"language\": \"en\"}");

		HttpUriRequest request = RequestBuilder.post().setUri(uriBuilder.build()).setEntity(requestEntity).addHeader(
			"Content-Type", "application/fhir+json").addHeader("Accept-Charset", "utf-8").addHeader(
				"Accept", "application/fhir+json; charset=utf-8").build();

		// Execute the request and process the results.
		HttpResponse response = httpClient.execute(request);
		HttpEntity responseEntity = response.getEntity();
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
			System.err.print(
				String.format("Exception creating FHIR resource: %s\n", response.getStatusLine().toString()));
			responseEntity.writeTo(System.err);
			throw new RuntimeException();
		}
		System.out.print("FHIR resource created: ");
		responseEntity.writeTo(System.out);
	}

	public static void authExplicit(String jsonPath) throws IOException {
		// You can specify a credential file by providing a path to GoogleCredentials.
		// Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath)).createScoped(
			Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
		Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

		System.out.println("Buckets:");
		Page<Bucket> buckets = storage.list();
		for (Bucket bucket : buckets.iterateAll()) {
			System.out.println(bucket.toString());
		}
	}

	private static CloudHealthcare createClient(String jsonPath) throws IOException {
		;
		// Use Application Default Credentials (ADC) to authenticate the requests
		// For more information see https://cloud.google.com/docs/authentication/production
		GoogleCredentials credential = GoogleCredentials.getApplicationDefault().createScoped(
			Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));
		// GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath)).createScoped(
		// Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

		// Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

		// System.out.println("Buckets:");
		// Page<Bucket> buckets = storage.list();
		// for (Bucket bucket : buckets.iterateAll()) {
		// System.out.println(bucket.toString());
		// }

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

	private static String getAccessToken() throws IOException {
		GoogleCredentials credential = GoogleCredentials.getApplicationDefault().createScoped(
			Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));

		return credential.refreshAccessToken().getTokenValue();
	}
}
// [END healthcare_create_resource]
