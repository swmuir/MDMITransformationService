/*******************************************************************************
 * Copyright (c) 2022 seanmuir.
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

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.jupiter.api.Test;

/**
 * @author seanmuir
 *
 */
class MPITest {

	// @Test
	// void testToken() throws ClientProtocolException, URISyntaxException, IOException {
	// System.out.println(FHIRR4PostProcessorJson.getAccessToken());
	// }

	@Test
	void testMPI() throws ClientProtocolException, URISyntaxException, IOException {

		// StringEscapeUtils.ESCAPE_JSON;

		System.out.println(StringEscapeUtils.escapeHtml4(StringEscapeUtils.escapeJson("\\\\AGANATHAN")));

		System.out.println(
			StringEscapeUtils.escapeHtml4(StringEscapeUtils.escapeJson("NEONATE, TRANSFERRED < 5 DAYS OLD, BORN HER")));

		// JSON.parse(str);

		// System.out.println(
		// FHIRR4PostProcessorJson.proccessMPI(FHIRR4PostProcessorJson.getAccessToken(), "doe2", "given", new Date()));
	}

}
