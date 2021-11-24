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
	void test() throws IOException, URISyntaxException {
		System.getenv();

		System.setProperty(
			"GOOGLE_APPLICATION_CREDENTIALS",
			"/Users/seanmuir/git/deletethis/MDMITransformationService/src/test/resources/mystic-sun-330921-66bbdbba8601.json");
		// FhirResourceCreate api = new FhirResourceCreate();

		// FhirResourceCreate.authExplicit("src/test/resources/mystic-sun-330921-66bbdbba8601.json");

		String fhirStoreName = String.format(
			FhirResourceCreate.FHIR_NAME, "mystic-sun-330921", "us-east4", "TestData", "TestData");
		String resourceType = "Patient";

		FhirResourceCreate.fhirResourceCreate(
			"src/test/resources/mystic-sun-330921-66bbdbba8601.json", fhirStoreName, resourceType);

	}

}
