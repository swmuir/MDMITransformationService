/*******************************************************************************
 * Copyright (c) 2021 seanmuir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     seanmuir - initial API and implementation
 *
 *******************************************************************************/
package org.mdmi.rt.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.mdmi.rt.service.web.FhirResourceCreate;

/**
 * @author seanmuir
 *
 */
class TestFhirClients {
	// String fhirStoreName =
	// String.format(
	// FHIR_NAME, "your-project-id", "your-region-id", "your-dataset-id", "your-fhir-id");
	// String resourceType = "Patient";

	//
	// https://healthcare.googleapis.com/v1/projects/mystic-sun-330921/locations/us-east4/datasets/TestData/fhirStores/TestData

	@Test
	void test01() throws IOException, URISyntaxException {

		String fhirStoreName = String.format(
			FhirResourceCreate.FHIR_NAME, "zanenet-njinck", "us-central1", "dev-zanenet-njinck", "dev-mdix-datastore");

		String content = Files.readString(
			Paths.get(
				"/Users/seanmuir/git/deletethis/MDMITransformationService/target/test-output/ADT_A01/ADT_A01.json"));
		;
		FhirResourceCreate.postBundle(
			"/Users/seanmuir/git/deletethis/MDMITransformationService/credentials/google_application_credentials.json",
			fhirStoreName, content);

	}

	@Test
	void test03() throws IOException, URISyntaxException {

		String fhirStoreName = String.format(
			FhirResourceCreate.FHIR_NAME, "zanenet-njinck", "us-central1", "dev-zanenet-njinck", "dev-mdix-datastore");

		String content = Files.readString(
			Paths.get(
				"/Users/seanmuir/git/deletethis/MDMITransformationService/target/test-output/ADT_A03/ADT_A03.json"));
		;
		FhirResourceCreate.postBundle(
			"/Users/seanmuir/git/deletethis/MDMITransformationService/credentials/google_application_credentials.json",
			fhirStoreName, content);

	}

	@Test
	void test04() throws IOException, URISyntaxException {

		String fhirStoreName = String.format(
			FhirResourceCreate.FHIR_NAME, "zanenet-njinck", "us-central1", "dev-zanenet-njinck", "dev-mdix-datastore");

		String content = Files.readString(
			Paths.get(
				"/Users/seanmuir/git/deletethis/MDMITransformationService/target/test-output/ADT_A04/ADT_A04.json"));
		;
		FhirResourceCreate.postBundle(
			"/Users/seanmuir/git/deletethis/MDMITransformationService/credentials/google_application_credentials.json",
			fhirStoreName, content);

	}

	@Test
	void test08() throws IOException, URISyntaxException {

		String fhirStoreName = String.format(
			FhirResourceCreate.FHIR_NAME, "zanenet-njinck", "us-central1", "dev-zanenet-njinck", "dev-mdix-datastore");

		String content = Files.readString(
			Paths.get(
				"/Users/seanmuir/git/deletethis/MDMITransformationService/target/test-output/ADT_A08/ADT_A08.json"));
		;
		FhirResourceCreate.postBundle(
			"/Users/seanmuir/git/deletethis/MDMITransformationService/credentials/google_application_credentials.json",
			fhirStoreName, content);

	}

	@Test
	void testQuery() throws IOException, URISyntaxException {

		String fhirStoreName = String.format(
			FhirResourceCreate.FHIR_NAME, "zanenet-njinck", "us-central1", "dev-zanenet-njinck", "dev-mdix-datastore");

		;
		String result = FhirResourceCreate.query(
			"/Users/seanmuir/git/deletethis/MDMITransformationService/credentials/google_application_credentials.json",
			fhirStoreName, "Patient?identifier=012002076302x");

		System.out.println(result);

	}

}
